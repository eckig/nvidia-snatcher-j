package model.store;

import java.util.Optional;

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
                        case RTX_3080_FE -> "https://www.notebooksbilliger.de/nvidia+geforce+rtx+3080+founders+edition+683301";
                        case RTX_3090_FE -> "https://www.notebooksbilliger.de/nvidia+geforce+rtx+3090+founders+edition+683300";
                    };
            return Optional.of(new StoreNotebooksbilliger(pModel, url));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Match> isInStock(final HtmlPage pHtmlPage)
    {
        final var status = pHtmlPage.getFirstByXPath("//div[@class='availability_widget']");
        if (status == null)
        {
            return Optional.empty();
        }
        final String statusText = safeText(status);
        final boolean isInStock = statusText.contains("lager") || statusText.contains("werktage");
        return Optional.of(isInStock ? Match.inStock(this, statusText) : Match.outOfStock(this, statusText));
    }
}
