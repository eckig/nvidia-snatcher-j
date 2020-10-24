package model;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import com.google.common.base.Strings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

public class Match
{
    private final LocalDateTime mTime = LocalDateTime.now();
    private final Search mSearch;
    private final String mMessage;
    private final State mState;

    private Match(final Search pSearch, final String pMessage, final State pState)
    {
        mMessage = pMessage;
        mSearch = pSearch;
        mState = pState;
    }

    public static Match inStock(final Search pSearch, final String pMessage)
    {
        return new Match(pSearch, pMessage, State.IN_STOCK);
    }

    public static Match outOfStock(final Search pSearch, final String pMessage)
    {
        return new Match(pSearch, pMessage, State.OUT_OF_STOCK);
    }

    public static Match unknown(final Search pSearch)
    {
        return new Match(pSearch, "Status could not be retrieved.", State.UNKNOWN);
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
        return mState == State.IN_STOCK;
    }

    public String consoleMessage()
    {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(mTime) + ": " +
                Strings.padEnd(search().store().name(), 15, ' ') + ": " +
                Ansi.colorize(mState.consoleName(), mState.ansiAttributes()) + ": " + search().model().name() + " (" +
                message() + ")";
    }

    public String notificationMessage()
    {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(mTime) + ": " + search().store() + ": " +
                search().model().name() + ": " + message();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final Match match = (Match) o;
        return Objects.equals(mTime, match.mTime) && Objects.equals(mSearch, match.mSearch) &&
                Objects.equals(mMessage, match.mMessage) && mState == match.mState;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mTime, mSearch, mMessage, mState);
    }

    @Override
    public String toString()
    {
        return "Match{" + "mTime=" + mTime + ", mSearch=" + mSearch + ", mMessage='" + mMessage + '\'' + ", mState=" +
                mState + '}';
    }

    private enum State
    {
        IN_STOCK("In Stock", Attribute.BOLD(), Attribute.GREEN_TEXT()),
        OUT_OF_STOCK("Out of Stock", Attribute.BOLD(), Attribute.RED_TEXT()),
        UNKNOWN("Unknown", Attribute.BOLD()),
        ERROR("Error", Attribute.BOLD(), Attribute.RED_TEXT());

        private final Attribute[] ansiAttributes;
        private final String name;

        State(final String pName, final Attribute... pAnsiAttributes)
        {
            ansiAttributes = pAnsiAttributes;
            name = pName;
        }

        Attribute[] ansiAttributes()
        {
            return ansiAttributes;
        }

        String consoleName()
        {
            return name;
        }
    }
}
