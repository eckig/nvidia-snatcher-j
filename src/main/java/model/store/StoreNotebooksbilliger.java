package model.store;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import model.Match;
import model.Search;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StoreNotebooksbilliger extends Search
{

    public enum Model
    {
        RTX_3080_FE("NVIDIA GEFORCE RTX 3080", "nvidia+geforce+rtx+3080+founders+edition"),
        RTX_3090_FE("NVIDIA GEFORCE RTX 3090", "nvidia+geforce+rtx+3090+founders+edition");

        private final String name;
        private final String gpu;

        Model(final String pName, final String pGpu)
        {
            name = pName;
            gpu = pGpu;
        }

        String model()
        {
            return name;
        }

        String url()
        {
            return "https://www.notebooksbilliger.de/" + gpu;
        }
    }

    public StoreNotebooksbilliger(final Model pModel)
    {
        super("NBB", Objects.requireNonNull(pModel.url(), "Model may not be null!"),
                Objects.requireNonNull(pModel.model(), "Store may not be null"), false);
    }

    @Override
    public <T> List<T> getListing(final HtmlPage pPage)
    {
        return pPage == null ? null : pPage.getByXPath("//div[@id='product_page_detail']");
    }

    @Override
    public <T> Optional<Match> matches(final List<T> pListing)
    {
        for (final var productDetailsListTile : pListing)
        {
            if (productDetailsListTile instanceof DomNode)
            {
                final DomNode htmlElement = (DomNode) productDetailsListTile;
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
                return Optional.of(match);
            }
        }
        return Optional.empty();
    }
}
