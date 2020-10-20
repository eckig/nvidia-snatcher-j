package main;

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
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import model.Search;
import notify.INotify;

public class JNvidiaSnatcher
{
    private static final String OUT_OF_STOCK = "Out Of Stock";

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
    }

    private void load()
    {
        try
        {
            final HtmlPage page = mWebClient.getPage(mSearch.getUrl());

            List<Object> productDetailsListTiles = null;
            for (int i = 0; i < 5 && (productDetailsListTiles == null || productDetailsListTiles.isEmpty()); i++)
            {
                mWebClient.waitForBackgroundJavaScript(200);
                productDetailsListTiles =
                        page.getByXPath(
                                "//div[@class='product-details-list-tile' or @class='product-container clearfix']");
            }

            for (final var productDetailsListTile : productDetailsListTiles)
            {
                if (productDetailsListTile instanceof DomNode)
                {
                    final DomNode htmlElement = (DomNode) productDetailsListTile;
                    for (final var name : htmlElement.getByXPath("//h2[@class='name']/text()"))
                    {
                        final var status = htmlElement.getFirstByXPath("//div[@class='buy']/a/text()");

                        if (name != null && (mSearch.getTitle().equals(name) ||
                                mSearch.getTitle().equals(name.toString())))
                        {
                            System.out.println(
                                    DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(LocalDateTime.now()) +
                                            ": " + name + ": " + status);
                            if (status == null || !OUT_OF_STOCK.equalsIgnoreCase(status.toString()))
                            {
                                notifyMatch();
                            }
                            return;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void notifyMatch()
    {
        for (final var notify : mToNotify)
        {
            notify.notify(mSearch);
        }
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

        //@formatter:off
        final List<Search> targets = List.of
        (
        new Search("https://www.nvidia.com/de-de/shop/geforce/gpu/?page=1&limit=9&locale=de-de&category=GPU&gpu=RTX%203080&manufacturer=NVIDIA","NVIDIA GEFORCE RTX 3080")
        //,new Search("https://www.nvidia.com/de-de/shop/geforce/gpu/?page=1&limit=9&locale=de-de&category=GPU&gpu=RTX%203090&manufacturer=NVIDIA","NVIDIA GEFORCE RTX 3090")
        );
        //@formatter:on

        final List<INotify> notify = INotify.fromEnvironment();
        final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        for (final var search : targets)
        {
            final JNvidiaSnatcher scraper = new JNvidiaSnatcher(search, notify);
            pool.scheduleWithFixedDelay(scraper::load, 15, 15, TimeUnit.SECONDS);
        }

        try
        {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
        }
    }

}
