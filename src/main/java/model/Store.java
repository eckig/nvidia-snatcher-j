package model;

import model.store.StoreNotebooksbilliger;
import model.store.StoreNvidia;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public enum Store
{
    NVIDIA_DE_DE("nvidia_de_de", Locale.forLanguageTag("de-de")),
    NVIDIA_EN_US("nvidia_en_us", Locale.forLanguageTag("en-us")),
    NBB("nbb", Locale.GERMAN);

    private final String mTag;
    private final Locale mLocale;

    Store(final String pTag, final Locale pLocale)
    {
        mTag = pTag;
        mLocale = pLocale;
    }

    public Stream<Search> createSearchFor(final List<Model> pModel)
    {
        if (pModel != null && !pModel.isEmpty())
        {
            return switch (this)
                    {
                        case NBB -> pModel.stream().flatMap(m -> StoreNotebooksbilliger.forModel(m).stream());
                        case NVIDIA_DE_DE, NVIDIA_EN_US -> pModel.stream()
                                .flatMap(m -> StoreNvidia.forModel(this, m, mLocale).stream());
                    };
        }
        return Stream.empty();
    }

    public static Optional<Store> forTag(final String pTag)
    {
        return Arrays.stream(Store.values()).filter(s -> s.mTag.equalsIgnoreCase(pTag)).findFirst();
    }
}
