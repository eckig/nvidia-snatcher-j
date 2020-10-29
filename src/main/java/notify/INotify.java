package notify;

import model.Search;
import util.Environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface INotify
{
    void notify(final Search pSearch, final String pMessage) throws IOException;

    static List<INotify> fromEnvironment()
    {
        final var list = new ArrayList<INotify>();
        final var gmailUser = Environment.get(GMailNotification.ENV_GMAIL_USER).orElse(null);
        final var gmailPw = Environment.get(GMailNotification.ENV_GMAIL_PASSWORD).orElse(null);
        if (gmailUser != null && gmailPw != null)
        {
            try
            {
                list.add(new GMailNotification(gmailUser, gmailPw));
            }
            catch (final Exception e)
            {
                System.out.println("Failed to create GMailNotifications: " + e);
            }
        }
        System.out.println("Notifications configured: " + list);
        return list;
    }
}
