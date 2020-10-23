package model;

import java.util.Objects;

public class Match
{
    private final Search mSearch;
    private final String mMessage;
    private final boolean mNotify;

    private Match(final Search pSearch, final String pMessage, final boolean pNotify)
    {
        mMessage = pMessage;
        mSearch = pSearch;
        mNotify = pNotify;
    }

    public static Match notify(final Search pSearch, final String pMessage)
    {
        return new Match(pSearch, pMessage, true);
    }

    public static Match info(final Search pSearch, final String pMessage)
    {
        return new Match(pSearch, pMessage, false);
    }

    public static Match unknown(final Search pSearch)
    {
        return new Match(pSearch, "Status could not be retrieved.", false);
    }

    public Search search()
    {
        return mSearch;
    }

    public String message()
    {
        return mMessage;
    }

    public boolean notification()
    {
        return mNotify;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return mNotify == match.mNotify && Objects.equals(mSearch, match.mSearch);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mSearch, mNotify);
    }

    @Override
    public String toString()
    {
        return "Match{" +
                "mSearch=" + mSearch +
                ", mNotify=" + mNotify +
                '}';
    }
}
