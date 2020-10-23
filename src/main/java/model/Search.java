package model;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class Search
{
    private final String url;
    private final String store;
    private final String title;

    public Search(final String pStore, final String pUrl, final String pTitle)
    {
        url = pUrl;
        title = pTitle;
        store = pStore;
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

    public abstract <T> List<T> getListing(final HtmlPage pHtmlPage);

    public abstract <T> Optional<Match> matches(final List<T> pListing);

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
        return Objects.equals(url, search.url) &&
                Objects.equals(title, search.title);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(url, title);
    }

    @Override
    public String toString()
    {
        return "Search{" +
                "url='" + url + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
