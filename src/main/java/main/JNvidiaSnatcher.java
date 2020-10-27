package main;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;

import model.Match;
import model.Search;
import notify.INotify;
import util.Environment;
import util.Pool;
import util.RateLimiters;
import util.html.SilentIncorrectnessListener;
import util.html.SynchronousAjaxController;

public class JNvidiaSnatcher
{
    public static final String ENV_SCRAPER_INTERVAL = "SCRAPER_INTERVAL";
    public static final String ENV_SCRAPER_PARALLELISM = "SCRAPER_PARALLELISM";
    public static final String ENV_SCRAPER_MAX_WAIT = "SCRAPER_MAX_WAIT";

    private final long mWaitTimeout;
    private final Search mSearch;
    private final List<INotify> mToNotify;
    private final ExecutorService mAsyncPool;
    private final Pool<WebClient> mWebClientPool;

    private JNvidiaSnatcher(final Search pSearch, final List<INotify> pToNotify, final long pWaitTimeout,
            final ExecutorService pAsyncPool, final Pool<WebClient> pWebClientPool)
    {
        mToNotify = pToNotify == null ? List.of() : List.copyOf(pToNotify);
        mSearch = Objects.requireNonNull(pSearch);
        mWaitTimeout = pWaitTimeout;
        mAsyncPool = pAsyncPool;
        mWebClientPool = pWebClientPool;
    }

    private static WebClient createWebClient()
    {
        final WebClient webClient = new WebClient();
        webClient.setAjaxController(SynchronousAjaxController.instance());
        webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
        webClient.setIncorrectnessListener(SilentIncorrectnessListener.instance());
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getOptions().setPopupBlockerEnabled(true);
        webClient.getOptions().setWebSocketEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setHistoryPageCacheLimit(0);
        webClient.getOptions().setHistorySizeLimit(-1);
        return webClient;
    }

    private CompletableFuture<Match> loadAsync(final WebClient pWebClient)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                final HtmlPage page = pWebClient.getPage(mSearch.url());
                Match match = mSearch.isInStock(page).orElse(null);
                if (mSearch.javascript())
                {
                    for (int i = 0; i < 5 && match == null; i++)
                    {
                        pWebClient.waitForBackgroundJavaScript(200);
                        match = mSearch.isInStock(page).orElse(null);
                    }
                }
                return match;
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }, mAsyncPool);
    }

    private void load()
    {
        try (final var pooledWebClient = mWebClientPool.get())
        {
            final WebClient webClient = pooledWebClient.element();
            webClient.getOptions().setJavaScriptEnabled(mSearch.javascript());
            
            Match result = null;
            try
            {
                result = loadAsync(webClient).get(mWaitTimeout, TimeUnit.SECONDS);
            }
            catch (final Exception e)
            {
                System.out.println("ERROR while scraping page: " + e);
            }

            notifyMatch(result == null ? Match.unknown(mSearch) : result);
            if (result == null)
            {
                pooledWebClient.destroyed();
                reset(webClient);
            }
        }
        catch (final Exception e)
        {
            // should not happen
            System.out.println("ERROR while waiting for pooled WebClient: " + e);
        }
    }

    private void reset(final WebClient pWebClient)
    {
        if (pWebClient != null)
        {
            // if nothing found, clear cache and cookies before next try:
            pWebClient.getCache().clear();
            pWebClient.getCookieManager().clearCookies();
            pWebClient.close();
        }
    }

    private void notifyMatch(final Match pMessage)
    {
        System.out.println(pMessage.consoleMessage());
        if (pMessage.notification())
        {
            for (final var notify : mToNotify)
            {
                try
                {
                    if (RateLimiters.get(notify.getClass(), pMessage.search().store(), pMessage.search().model())
                            .tryAcquire())
                    {
                        notify.notify(mSearch, pMessage.notificationMessage());
                    }
                }
                catch (final IOException e)
                {
                    System.out.println("Failed to notify about match:");
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    public static void main(final String[] args)
    {
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        final var interval = Environment.getLong(ENV_SCRAPER_INTERVAL, 20);
        final var waitTimeout = Environment.getLong(ENV_SCRAPER_MAX_WAIT, interval * 2);
        final var parallelism = Environment.getInt(ENV_SCRAPER_PARALLELISM, 2);

        System.out.println(ENV_SCRAPER_INTERVAL + "=" + interval);
        System.out.println(ENV_SCRAPER_MAX_WAIT + "=" + waitTimeout);
        System.out.println(ENV_SCRAPER_PARALLELISM + "=" + parallelism);

        final var targets = Search.fromEnvironment();
        final var notify = INotify.fromEnvironment();

        // dont use fixed thread pool as a thread might get stuck (timeout, etc.)
        final var asyncPool = Executors.newCachedThreadPool();
        final var schedulePool = Executors.newScheduledThreadPool(parallelism);
        final var webClientPool = new Pool<>(parallelism, JNvidiaSnatcher::createWebClient);

        for (final var search : targets)
        {
            final JNvidiaSnatcher scraper = new JNvidiaSnatcher(search, notify, waitTimeout, asyncPool, webClientPool);
            schedulePool.scheduleWithFixedDelay(scraper::load, 0, interval, TimeUnit.SECONDS);
        }

        try
        {
            schedulePool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (final InterruptedException e)
        {
            System.out.println("Interrupted!");
        }
    }

}
