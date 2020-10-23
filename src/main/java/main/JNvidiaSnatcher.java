package main;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import model.Match;
import model.NotebooksbilligerStoreSearch;
import model.NvidiaStoreSearch;
import model.Search;
import notify.INotify;

public class JNvidiaSnatcher
{
    public static final String ENV_SCRAPER_INTERVAL = "SCRAPER_INTERVAL";

    private final WebClient mWebClient = new WebClient();
    private final Search mSearch;
    private final List<INotify> mToNotify;

    private JNvidiaSnatcher(final Search pSearch, final List<INotify> pToNotify)
    {
        mToNotify = pToNotify == null ? List.of() : List.copyOf(pToNotify);
        mSearch = Objects.requireNonNull(pSearch);
        mWebClient.setAjaxController(new AjaxController()
        {
            @Override
            public boolean processSynchron(final HtmlPage page, final WebRequest request, final boolean async)
            {
                return true;
            }
        });
        mWebClient.getOptions().setCssEnabled(false);
        mWebClient.getOptions().setJavaScriptEnabled(true);
        mWebClient.getOptions().setDownloadImages(false);
        mWebClient.getOptions().setAppletEnabled(false);
        mWebClient.getOptions().setPopupBlockerEnabled(true);
        mWebClient.getOptions().setWebSocketEnabled(false);
        mWebClient.getOptions().setThrowExceptionOnScriptError(false);
        mWebClient.getOptions().setHistoryPageCacheLimit(0);
        mWebClient.getOptions().setHistorySizeLimit(-1);
    }

    private void load()
    {
        try
        {
            final HtmlPage page = mWebClient.getPage(mSearch.url());

            List<Object> listing = null;
            for (int i = 0; i < 5 && (listing == null || listing.isEmpty()); i++)
            {
                mWebClient.waitForBackgroundJavaScript(200);
                listing = mSearch.getListing(page);
            }

            mSearch.matches(listing).ifPresentOrElse(this::notifyMatch, this::reset);
        }
        catch (Exception e)
        {
            System.out.println("ERROR while scraping page: ");
            e.printStackTrace(System.out);
        }
    }

    private void reset()
    {
        // if nothing found, clear cache and cookies before next try:
        mWebClient.getCache().clear();
        mWebClient.getCookieManager().clearCookies();
    }

    private void notifyMatch(final Match pMessage)
    {
        final String messageWithTime =
                DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(LocalDateTime.now()) + ": " +
                        pMessage.search().store() + ": " + pMessage.search().product() + ": " + pMessage.message();
        System.out.println(messageWithTime);
        if (!pMessage.notification())
        {
            return;
        }
        for (final var notify : mToNotify)
        {
            try
            {
                notify.notify(mSearch, messageWithTime);
            }
            catch (IOException e)
            {
                System.out.println("Failed to notify about match:");
                e.printStackTrace(System.out);
            }
        }
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        final String envInterval = System.getenv(ENV_SCRAPER_INTERVAL);
        final long interval = envInterval == null || envInterval.isBlank() ? 20 : Long.parseLong(envInterval);

        final List<Search> targets =
                List.of(new NvidiaStoreSearch(NvidiaStoreSearch.Model.RTX_3080_FE, NvidiaStoreSearch.Store.NVIDIA_DE_DE),
                        new NotebooksbilligerStoreSearch(NotebooksbilligerStoreSearch.Model.RTX_3080_FE));

        final List<INotify> notify = INotify.fromEnvironment();
        final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        for (final var search : targets)
        {
            final JNvidiaSnatcher scraper = new JNvidiaSnatcher(search, notify);
            pool.scheduleWithFixedDelay(scraper::load, 0, interval, TimeUnit.SECONDS);
        }

        try
        {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            System.out.println("Interrupted!");
        }
    }

}
