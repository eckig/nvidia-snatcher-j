package model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import util.Environment;

public abstract class Search
{
    public static final String ENV_STORES = "SCRAPER_STORES";
    public static final String ENV_MODELS = "SCRAPER_MODELS";

    private final String url;
    private final Store store;
    private final Model model;
    private final boolean javascript;

    public Search(final Store pStore, final String pUrl, final Model pModel, final boolean pJavascript)
    {
        url = Objects.requireNonNull(pUrl, "URL may not be null!");
        model = Objects.requireNonNull(pModel, "Model may not be null!");
        store = Objects.requireNonNull(pStore, "Store may not be null!");
        javascript = pJavascript;
    }

    public String url()
    {
        return url;
    }

    public Model model()
    {
        return model;
    }

    public Store store()
    {
        return store;
    }

    public boolean javascript()
    {
        return javascript;
    }

    public abstract Optional<Match> isInStock(final HtmlPage pHtmlPage);

    @Override
    public boolean equals(final Object pO)
    {
        if (this == pO)
        {
            return true;
        }
        if (pO == null || getClass() != pO.getClass())
        {
            return false;
        }
        final Search search = (Search) pO;
        return Objects.equals(url, search.url) && Objects.equals(model, search.model);
    }

    protected static String safeText(final Object pNode)
    {
        if (pNode == null)
        {
            return "";
        }
        final String text;
        if (pNode instanceof DomNode)
        {
            text = ((DomNode) pNode).asText();
        }
        else
        {
            text = pNode.toString();
        }
        return text.replace("[]", "").strip();
    }

    public static List<Search> fromEnvironment()
    {
        final var models = Environment.getList(ENV_MODELS)
                .stream()
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .flatMap(s -> Model.forTag(s).stream())
                .collect(Collectors.toList());
        final var searches = Environment.getList(ENV_STORES)
                .stream()
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .flatMap(s -> Store.forTag(s).stream())
                .flatMap(s -> s.createSearchFor(models))
                .collect(Collectors.toList());
        System.out.println("Search configured for: " + searches);
        return searches;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(url, model);
    }

    @Override
    public String toString()
    {
        return "Search{" + "url='" + url + '\'' + ", title='" + model + '\'' + '}';
    }
}
