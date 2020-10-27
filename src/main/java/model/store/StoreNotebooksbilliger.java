package model.store;

import java.util.List;
import java.util.Optional;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import model.Match;
import model.Model;
import model.Search;
import model.Store;

public class StoreNotebooksbilliger extends Search
{

    private StoreNotebooksbilliger(final Model pModel, final String pUrl)
    {
        super(Store.NBB, pUrl, pModel, false);
    }

    public static Optional<Search> forModel(final Model pModel)
    {
        if (pModel != null)
        {
            final String url = switch (pModel)
                    {
                        case RTX_3070_FE -> "https://www.notebooksbilliger.de/nvidia+geforce+rtx+3070+founders+edition";
                        case RTX_3080_FE -> "https://www.notebooksbilliger.de/nvidia+geforce+rtx+3080+founders+edition";
                        case RTX_3090_FE -> "https://www.notebooksbilliger.de/nvidia+geforce+rtx+3090+founders+edition";
                    };
            return Optional.of(new StoreNotebooksbilliger(pModel, url));
        }
        return Optional.empty();
    }

    @Override
    public <T> List<T> getListing(final HtmlPage pPage)
    {
        return pPage == null ? null : pPage.getByXPath("//div[@id='product_page_detail']");
    }

    @Override
    public <T> Match matches(final List<T> pListing)
    {
        for (final var productDetailsListTile : pListing)
        {
            if (productDetailsListTile instanceof DomNode)
            {
                final var htmlElement = (DomNode) productDetailsListTile;
                final var status = htmlElement.getFirstByXPath("//div[@class='availability_widget']");
                final Match match;
                if (status == null)
                {
                    match = Match.unknown(this);
                }
                else
                {
                    final String statusText = safeText(status);
                    final boolean isInStock = statusText.contains("sofort ab lager");
                    match = isInStock ? Match.inStock(this, statusText) : Match.outOfStock(this, statusText);
                }
                return match;
            }
        }
        return Match.unknown(this);
    }
}
