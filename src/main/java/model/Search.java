package model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import util.Environment;

public abstract class Search
{
    public static final String ENV_STORES = "SCRAPER_STORES";
    public static final String ENV_MODELS = "SCRAPER_MODELS";

    private final String mUrl;
    private final Store mStore;
    private final Model mModel;
    private final boolean mJavascript;

    private final AtomicReference<Match> mLastMatch = new AtomicReference<>();

    public Search(final Store pStore, final String pUrl, final Model pModel, final boolean pJavascript)
    {
        mUrl = Objects.requireNonNull(pUrl, "URL may not be null!");
        mModel = Objects.requireNonNull(pModel, "Model may not be null!");
        mStore = Objects.requireNonNull(pStore, "Store may not be null!");
        mJavascript = pJavascript;
    }

    public String url()
    {
        return mUrl;
    }

    public Model model()
    {
        return mModel;
    }

    public Store store()
    {
        return mStore;
    }

    public boolean javascript()
    {
        return mJavascript;
    }

    public Match lastMatch(final Match pLastMatch)
    {
        return mLastMatch.getAndSet(pLastMatch);
    }

    public abstract Optional<Match> isInStock(final HtmlPage pHtmlPage);

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
        return text.strip();
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
    public String toString()
    {
        return "Search{" + "store=" + mStore + ", model=" + mModel + '}';
    }
}
