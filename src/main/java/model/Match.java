package model;

import java.util.Objects;

public class Match
{
    private final String mMessage;
    private final boolean mNotify;

    private Match(final String pMessage, final boolean pNotify)
    {
        mMessage = pMessage;
        mNotify = pNotify;
    }

    public static Match notify(final String pMessage)
    {
        return new Match(pMessage, true);
    }

    public static Match info(final String pMessage)
    {
        return new Match(pMessage, false);
    }

    public static Match unknown(final Search pSearch)
    {
        return new Match(pSearch.getTitle() + ": Status could not be retrieved.", false);
    }

    public String getMessage()
    {
        return mMessage;
    }

    public boolean isNotify()
    {
        return mNotify;
    }

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
        final Match match = (Match) pO;
        return mNotify == match.mNotify &&
                Objects.equals(mMessage, match.mMessage);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mMessage, mNotify);
    }

    @Override
    public String toString()
    {
        return "Match{" +
                "mMessage='" + mMessage + '\'' +
                ", mNotify=" + mNotify +
                '}';
    }
}
