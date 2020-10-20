package model;

public class Search
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
}
