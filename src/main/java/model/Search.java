package model;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.List;
import java.util.Optional;

public abstract class Search
{
    private final String url;
    private final String title;

    public Search(final String pUrl, final String pTitle)
    {
        url = pUrl;
        title = pTitle;
    }

    public String getUrl()
    {
        return url;
    }

    public String getTitle()
    {
        return title;
    }

    public abstract <T> List<T> getListing(final HtmlPage pHtmlPage);

    public abstract <T> Optional<Match> matches(final List<T> pListing);
}
