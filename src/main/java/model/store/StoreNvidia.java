package model.store;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import model.Match;
import model.Model;
import model.Search;
import model.Store;

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
                    case RTX_3070_FE -> "RTX%203070";
                    case RTX_3080_FE -> "RTX%203080";
                    case RTX_3090_FE -> "RTX%203090";
                };
        final String locale = pLocale.toLanguageTag().toLowerCase();
        return "https://www.nvidia.com/" + locale + "/shop/geforce/gpu/?page=1&limit=1&locale=" + locale +
                "&category=GPU&gpu=" + gpu + "&manufacturer=NVIDIA";
    }

    @Override
    public Optional<Match> isInStock(final HtmlPage pHtmlPage)
    {
        final Object statusElement = pHtmlPage.getFirstByXPath("//div[@class='buy']");
        if (statusElement == null)
        {
            return Optional.empty();
        }
        final var status = safeText(statusElement);
        return Optional.of(isInStock(status) ? Match.inStock(this, status) : Match.outOfStock(this, status));
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
