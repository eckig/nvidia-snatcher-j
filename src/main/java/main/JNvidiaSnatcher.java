package main;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;

import model.Match;
import model.Search;
import util.Environment;
import util.Pool;
import util.html.SilentIncorrectnessListener;
import util.html.SynchronousAjaxController;

public class JNvidiaSnatcher
{
    public static final String ENV_LOAD_INTERVAL = "LOAD_INTERVAL";
    public static final String ENV_LOAD_PARALLELISM = "LOAD_PARALLELISM";
    public static final String ENV_LOAD_MAX_WAIT = "LOAD_MAX_WAIT";
    public static final String ENV_NOTIFY_ON_CHANGE = "NOTIFY_ON_CHANGE";

    private JNvidiaSnatcher()
    {
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

    private static CompletableFuture<Match> loadAsync(final WebClient pWebClient, final Search pSearch,
            final Executor pExecutor)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                final HtmlPage page = pWebClient.getPage(pSearch.url());
                Match match = pSearch.isInStock(page).orElse(null);
                if (pSearch.javascript())
                {
                    for (int i = 0; i < 5 && match == null; i++)
                    {
                        pWebClient.waitForBackgroundJavaScript(200);
                        match = pSearch.isInStock(page).orElse(null);
                    }
                }
                return match;
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }, pExecutor);
    }

    private static void load(final Search pSearch, final ScraperEnvironment pEnvironment)
    {
        try (final var pooledWebClient = pEnvironment.webClientPool().get())
        {
            final WebClient webClient = pooledWebClient.element();
            webClient.getOptions().setJavaScriptEnabled(pSearch.javascript());

            Match result = null;
            try
            {
                result = loadAsync(webClient, pSearch, pEnvironment.asyncPool()).get(pEnvironment.waitTimeout(),
                        TimeUnit.SECONDS);
            }
            catch (final Exception e)
            {
                System.out.println("ERROR while scraping page: " + e);
            }

            notifyMatch(result == null ? Match.unknown(pSearch) : result, pSearch, pEnvironment);
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

    private static void reset(final WebClient pWebClient)
    {
        if (pWebClient != null)
        {
            // if nothing found, clear cache and cookies before next try:
            pWebClient.getCache().clear();
            pWebClient.getCookieManager().clearCookies();
            pWebClient.close();
        }
    }

    private static void notifyMatch(final Match pMessage, final Search pSearch, final ScraperEnvironment pEnvironment)
    {
        System.out.println(pMessage.consoleMessage());

        final var last = pSearch.lastMatch(pMessage);
        boolean notified = true;

        if (pMessage.inStock() || (pEnvironment.notifyOnStatusChanged() && notifyOnChange(pMessage, last)))
        {
            for (final var notify : pEnvironment.notifiers())
            {
                try
                {
                    notify.notify(pSearch, pMessage.notificationMessage());
                }
                catch (final IOException e)
                {
                    notified = false;
                    System.out.println("Failed to notify about match:");
                    e.printStackTrace(System.out);
                }
            }
        }

        if (pMessage.inStock() && notified)
        {
            // if the "in stock" notification has been sent successfully send this search to sleep for a while:
            try
            {
                Thread.sleep(60000);
            }
            catch (InterruptedException pE)
            {
                // sleep interrupted
            }
        }
    }

    private static boolean notifyOnChange(final Match pNow, final Match pPrevious)
    {
        if (pNow == null || pPrevious == null || pPrevious.unknown() || pNow.unknown())
        {
            return false;
        }
        return pPrevious.inStock() != pNow.inStock() || !Objects.equals(pNow.message(), pPrevious.message());
    }

    public static void main(final String[] args)
    {
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        final var interval = Environment.getLong(ENV_LOAD_INTERVAL, 20);
        final var waitTimeout = Environment.getLong(ENV_LOAD_MAX_WAIT, interval * 2);
        final var parallelism = Environment.getInt(ENV_LOAD_PARALLELISM, 2);
        final var notifyOnChange = Environment.getBoolean(ENV_NOTIFY_ON_CHANGE, false);

        System.out.println(ENV_LOAD_INTERVAL + "=" + interval);
        System.out.println(ENV_LOAD_MAX_WAIT + "=" + waitTimeout);
        System.out.println(ENV_LOAD_PARALLELISM + "=" + parallelism);
        System.out.println(ENV_NOTIFY_ON_CHANGE + "=" + notifyOnChange);

        final var targets = Search.fromEnvironment();
        final var schedulePool = Executors.newScheduledThreadPool(parallelism);
        final var webClientPool = new Pool<>(parallelism, JNvidiaSnatcher::createWebClient);
        final var environment = new ScraperEnvironment(waitTimeout, notifyOnChange, webClientPool);

        for (final var search : targets)
        {
            schedulePool.scheduleWithFixedDelay(() -> load(search, environment), 0, interval, TimeUnit.SECONDS);
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
