package model;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.net.UrlEscapers;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class NvidiaStoreSearch extends Search
{

    public enum Model
    {
        RTX_3080_FE("NVIDIA GEFORCE RTX 3080", "RTX 3080", "NVIDIA"),
        RTX_3090_FE("NVIDIA GEFORCE RTX 3090", "RTX 3090", "NVIDIA");

        private final String name;
        private final String gpu;
        private final String manufacturer;

        Model(final String pName, final String pGpu, final String pManufacturer)
        {
            name = pName;
            gpu = pGpu;
            manufacturer = pManufacturer;
        }

        String model()
        {
            return name;
        }

        String url(final Store pStore)
        {
            return "https://www.nvidia.com/" + pStore.localeUrl() + "/shop/geforce/gpu/?page=1&limit=9&locale=" +
                    pStore.localeUrl() + "&category=GPU&gpu=" + UrlEscapers.urlFragmentEscaper().escape(gpu) +
                    "&manufacturer=" + UrlEscapers.urlFragmentEscaper().escape(manufacturer);
        }
    }

    public enum Store
    {
        DE_DE("jetzt kaufen", "de-de", Locale.GERMAN),
        EN_US("buy now", "en-us", Locale.ENGLISH);

        private final String inStockText;
        private final String localeUrl;
        private final Locale language;

        Store(final String pInStockText, final String pLocaleUrl, final Locale pLanguage)
        {
            inStockText = pInStockText;
            localeUrl = pLocaleUrl;
            language = pLanguage;
        }

        boolean isInStock(final String pStatus)
        {
            return pStatus != null && pStatus.toLowerCase(language).contains(inStockText);
        }

        public String localeUrl()
        {
            return localeUrl;
        }
    }

    private final Store mStore;

    public NvidiaStoreSearch(final Model pModel, final Store pStore)
    {
        super(Objects.requireNonNull(pModel.url(pStore), "Model may not be null!"),
                Objects.requireNonNull(pModel.model(), "Store may not be null"));
        mStore = pStore;
    }

    @Override
    public <T> List<T> getListing(HtmlPage pPage)
    {
        return pPage == null ? null : pPage.getByXPath("//div[@class='product-details-list-tile' or @class='product-container clearfix']");
    }

    @Override
    public <T> Optional<Match> matches(List<T> pListing)
    {
        for (final var productDetailsListTile : pListing)
        {
            if (productDetailsListTile instanceof DomNode)
            {
                final DomNode htmlElement = (DomNode) productDetailsListTile;
                for (final var name : htmlElement.getByXPath("//h2[@class='name']/text()"))
                {
                    if (name != null && (getTitle().equals(name) || getTitle().equals(name.toString())))
                    {
                        final var status = htmlElement.getFirstByXPath("//div[@class='buy']/a/text()");
                        final Match match;
                        if (status == null)
                        {
                            match = Match.unknown(this);
                        }
                        else
                        {
                            final String message = getTitle() + ": " + status;
                            final boolean isInStock = mStore.isInStock(safeText(status.toString()));
                            match = isInStock ? Match.notify(message) : Match.info(message);
                        }
                        return Optional.of(match);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static String safeText(final String pText)
    {
        if (pText == null)
        {
            return "";
        }
        return pText.strip();
    }
}
