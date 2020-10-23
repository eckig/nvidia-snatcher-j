package model;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.List;
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
            return "https://www.nvidia.com/" + pStore.locale() + "/shop/geforce/gpu/?page=1&limit=9&locale=" +
                    pStore.locale + "&category=GPU&gpu="+gpu+"&manufacturer="+manufacturer;
        }
    }

    public enum Store
    {
        DE_DE("Derzeit nicht verfügbar", "de-de"),
        EN_US("Out Of Stock", "en-us");

        private final String outOfStockText;
        private final String locale;

        Store(final String pOutOfStockText, final String pLocale)
        {
            outOfStockText = pOutOfStockText;
            locale = pLocale;
        }

        String outOfStockText()
        {
            return outOfStockText;
        }

        public String locale()
        {
            return locale;
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
                        final String message = name + ": " + status;
                        final Match match =
                                status == null || !mStore.outOfStockText().equalsIgnoreCase(status.toString()) ?
                                        Match.notify(message) : Match.info(message);
                        return Optional.of(match);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
