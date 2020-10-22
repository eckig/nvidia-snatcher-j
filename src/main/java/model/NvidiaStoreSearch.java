package model;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class NvidiaStoreSearch extends Search
{
    private static final String OUT_OF_STOCK = "Out Of Stock";

    public NvidiaStoreSearch(String pUrl, String pTitle)
    {
        super(pUrl, pTitle);
    }

    @Override
    public <T> List<T> getListing(HtmlPage pPage)
    {
        return pPage == null ? null : pPage.getByXPath("//div[@class='product-details-list-tile' or @class='product-container clearfix']");
    }

    @Override
    public <T> boolean matches(List<T> pListing)
    {
        for (final var productDetailsListTile : pListing)
        {
            if (productDetailsListTile instanceof DomNode)
            {
                final DomNode htmlElement = (DomNode) productDetailsListTile;
                for (final var name : htmlElement.getByXPath("//h2[@class='name']/text()"))
                {
                    final var status = htmlElement.getFirstByXPath("//div[@class='buy']/a/text()");

                    if (name != null && (getTitle().equals(name) || getTitle().equals(name.toString())))
                    {
                        System.out.println(
                                DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(LocalDateTime.now()) +
                                        ": " + name + ": " + status);
                        return status == null || !OUT_OF_STOCK.equalsIgnoreCase(status.toString());
                    }
                }
            }
        }
        return false;
    }
}
