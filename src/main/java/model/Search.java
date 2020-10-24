package model;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class Search
{
    private final String url;
    private final String store;
    private final String title;
    private final boolean javascript;

    public Search(final String pStore, final String pUrl, final String pTitle, final boolean pJavascript)
    {
        url = pUrl;
        title = pTitle;
        store = pStore;
        javascript = pJavascript;
    }

    public String url()
    {
        return url;
    }

    public String product()
    {
        return title;
    }

    public String store()
    {
        return store;
    }

    public boolean javascript()
    {
        return javascript;
    }

    public abstract <T> List<T> getListing(final HtmlPage pHtmlPage);

    public abstract <T> Optional<Match> matches(final List<T> pListing);

    @Override public boolean equals(final Object pO)
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
        return Objects.equals(url, search.url) && Objects.equals(title, search.title);
    }

    protected static String safeText(final Object pNode)
    {
        if (pNode instanceof DomNode)
        {
            return safeText(((DomNode) pNode).asText());
        }
        return pNode == null ? "" : safeText(pNode.toString());
    }

    protected static String safeText(final String pText)
    {
        if (pText == null)
        {
            return "";
        }
        return pText.strip();
    }

    @Override public int hashCode()
    {
        return Objects.hash(url, title);
    }

    @Override public String toString()
    {
        return "Search{" + "url='" + url + '\'' + ", title='" + title + '\'' + '}';
    }
}
