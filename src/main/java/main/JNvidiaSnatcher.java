package main;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;
import model.Match;
import model.Search;
import model.store.StoreNotebooksbilliger;
import model.store.StoreNvidia;
import notify.INotify;
import util.html.SilentIncorrectnessListener;
import util.html.SynchronousAjaxController;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JNvidiaSnatcher
{
    public static final String ENV_SCRAPER_INTERVAL = "SCRAPER_INTERVAL";

    private final long mWaitTimeout;
    private final Search mSearch;
    private final List<INotify> mToNotify;

    private WebClient mWebClient;

    private JNvidiaSnatcher(final Search pSearch, final List<INotify> pToNotify, final long pWaitTimeout)
    {
        mToNotify = pToNotify == null ? List.of() : List.copyOf(pToNotify);
        mSearch = Objects.requireNonNull(pSearch);
        mWaitTimeout = pWaitTimeout;
    }

    private static WebClient createWebClient(final Search pSearch)
    {
        final WebClient webClient = new WebClient();
        if (pSearch.javascript())
        {
            webClient.setAjaxController(SynchronousAjaxController.instance());
            webClient.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
            webClient.getOptions().setJavaScriptEnabled(true);
        }
        else
        {
            webClient.getOptions().setJavaScriptEnabled(false);
        }
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

    private WebClient getWebClient()
    {
        if (mWebClient == null)
        {
            mWebClient = createWebClient(mSearch);
        }
        return mWebClient;
    }

    private CompletableFuture<Match> loadAsync(final WebClient pWebClient)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                final HtmlPage page = pWebClient.getPage(mSearch.url());

                List<Object> listing = null;
                if (mSearch.javascript())
                {
                    for (int i = 0; i < 5 && (listing == null || listing.isEmpty()); i++)
                    {
                        pWebClient.waitForBackgroundJavaScript(200);
                        listing = mSearch.getListing(page);
                    }
                }
                else
                {
                    listing = mSearch.getListing(page);
                }

                return listing == null ? null : mSearch.matches(listing).orElse(null);
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    private void load()
    {
        Match result = null;
        try
        {
            result = loadAsync(getWebClient()).get(mWaitTimeout, TimeUnit.SECONDS);
        }
        catch (final Exception e)
        {
            System.out.println("ERROR while scraping page: " + e);
        }

        if (result != null)
        {
            notifyMatch(result);
        }
        else
        {
            reset();
        }
    }

    private void reset()
    {
        final var client = mWebClient;
        if (client != null)
        {
            // if nothing found, clear cache and cookies before next try:
            client.getCache().clear();
            client.getCookieManager().clearCookies();
            client.close();
        }
        mWebClient = null;
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
                    notify.notify(mSearch, pMessage.notificationMessage());
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

        final String envInterval = System.getenv(ENV_SCRAPER_INTERVAL);
        final long interval = envInterval == null || envInterval.isBlank() ? 20 : Long.parseLong(envInterval);
        final long waitTimeout = interval * 2;

        final List<Search> targets =
                List.of(new StoreNvidia(StoreNvidia.Model.RTX_3080_FE, StoreNvidia.Store.NVIDIA_DE_DE),
                        new StoreNotebooksbilliger(StoreNotebooksbilliger.Model.RTX_3080_FE));

        final List<INotify> notify = INotify.fromEnvironment();
        final ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        for (final var search : targets)
        {
            final JNvidiaSnatcher scraper = new JNvidiaSnatcher(search, notify, waitTimeout);
            pool.scheduleWithFixedDelay(scraper::load, 0, interval, TimeUnit.SECONDS);
        }

        try
        {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (final InterruptedException e)
        {
            System.out.println("Interrupted!");
        }
    }

}
