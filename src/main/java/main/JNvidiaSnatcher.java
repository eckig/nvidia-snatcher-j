package main;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;

import model.Match;
import model.Search;
import util.Pool;
import util.html.SilentIncorrectnessListener;
import util.html.SynchronousAjaxController;

public class JNvidiaSnatcher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JNvidiaSnatcher.class);

    private JNvidiaSnatcher()
    {
    }

    private static WebClient createWebClient(final Environment pEnvironment)
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
        pEnvironment.proxyConfig().ifPresent(webClient.getOptions()::setProxyConfig);
        return webClient;
    }

    private static Future<Match> loadAsync(final WebClient pWebClient, final Search pSearch,
            final ExecutorService pExecutor)
    {
        return pExecutor.submit(() ->
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
            catch (FailingHttpStatusCodeException e)
            {
                return Match.invalidHttpStatus(pSearch, e.getMessage());
            }
        });
    }

    private static void load(final Search pSearch, final Environment pEnvironment, final Pool<WebClient> pPool,
            final ExecutorService pAsyncPool)
    {
        final LocalTime now = LocalTime.now();
        if (now.isBefore(pEnvironment.timeFrom()) || now.isAfter(pEnvironment.timeTo()))
        {
            return;
        }

        Match result = null;
        try (final var pooledWebClient = pPool.get())
        {
            final WebClient webClient = pooledWebClient.element();
            webClient.getOptions().setJavaScriptEnabled(pSearch.javascript());

            try
            {
                result = loadAsync(webClient, pSearch, pAsyncPool).get(pEnvironment.waitTimeout(), TimeUnit.SECONDS);
            }
            catch (final Exception e)
            {
                LOGGER.error("ERROR while scraping page: ", e);
            }

            if (result == null)
            {
                pooledWebClient.destroyed();
                reset(webClient);
            }
        }
        catch (final Exception e)
        {
            // should not happen
            LOGGER.error("ERROR while waiting for pooled WebClient: ", e);
        }

        try
        {
            notifyMatch(result == null ? Match.unknown(pSearch) : result, pSearch, pEnvironment);
        }
        catch (final Exception e)
        {
            // should not happen
            LOGGER.error("ERROR while sending notifications: ", e);
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

    private static void notifyMatch(final Match pMessage, final Search pSearch, final Environment pEnvironment)
    {
        LOGGER.info(pMessage.consoleMessage());

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
                    LOGGER.error("Failed to notify about match:", e);
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
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);

        final var environment = Environment.fromSystemEnv();
        final var schedulePool = Executors.newScheduledThreadPool(environment.searches().size());
        final var asyncPool = Executors.newCachedThreadPool();
        final var webClientPool = new Pool<>(environment.parallelism(), () -> createWebClient(environment));

        for (final var search : environment.searches())
        {
            schedulePool.scheduleWithFixedDelay(() -> load(search, environment, webClientPool, asyncPool), 0,
                    environment.interval(), TimeUnit.SECONDS);
        }

        try
        {
            schedulePool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (final InterruptedException e)
        {
            LOGGER.error("Process interrupted!");
        }
    }

}
