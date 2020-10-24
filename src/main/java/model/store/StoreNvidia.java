package model.store;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import model.Match;
import model.Model;
import model.Search;
import model.Store;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class StoreNvidia extends Search
{

    private final Locale mLocale;

    private StoreNvidia(final Model pModel, final Store pStore, final Locale pLocale)
    {
        super(pStore, urlFor(pLocale, pModel), pModel, true);
        mLocale = Objects.requireNonNull(pLocale, "Locale may not be null!");
    }

    public static Optional<Search> forModel(final Store pStore, final Model pModel, final Locale pLocale)
    {
        if (pModel != null)
        {
            return Optional.of(new StoreNvidia(pModel, pStore, pLocale));
        }
        return Optional.empty();
    }

    private static String urlFor(final Locale pLocale, final Model pModel)
    {
        Objects.requireNonNull(pLocale, "Locale may not be null!");
        Objects.requireNonNull(pModel, "Model may not be null!");
        final String gpu = switch (pModel)
                {
                    case RTX_3080_FE -> "RTX%203080";
                    case RTX_3090_FE -> "RTX%203090";
                };
        final String locale = pLocale.toLanguageTag().toLowerCase();
        return "https://www.nvidia.com/" + locale + "/shop/geforce/gpu/?page=1&limit=9&locale=" + locale +
                "&category=GPU&gpu=" + gpu + "&manufacturer=NVIDIA";
    }

    @Override
    public <T> List<T> getListing(final HtmlPage pPage)
    {
        return pPage == null ? null :
                pPage.getByXPath("//div[@class='product-details-list-tile' or @class='product-container clearfix']");
    }

    @Override
    public <T> Optional<Match> matches(final List<T> pListing)
    {
        for (final var productDetailsListTile : pListing)
        {
            if (productDetailsListTile instanceof DomNode)
            {
                final DomNode htmlElement = (DomNode) productDetailsListTile;
                final var status = htmlElement.getFirstByXPath("//div[@class='buy']/a/text()");
                final Match match;
                if (status == null)
                {
                    match = Match.unknown(this);
                }
                else
                {
                    final boolean isInStock = isInStock(safeText(status.toString()));
                    match = isInStock ? Match.inStock(this, status.toString()) :
                            Match.outOfStock(this, status.toString());
                }
                return Optional.of(match);
            }
        }
        return Optional.empty();
    }

    private boolean isInStock(final String pStatus)
    {
        final String inStockText = switch (store())
                {
                    case NVIDIA_DE_DE -> "jetzt kaufen";
                    case NVIDIA_EN_US -> "buy now";
                    default -> "";
                };
        return pStatus != null && pStatus.toLowerCase(mLocale).contains(inStockText);
    }
}
