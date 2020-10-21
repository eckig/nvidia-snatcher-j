package notify;

import model.Search;

import java.io.IOException;
import java.util.List;

public interface INotify
{
    void notify(final Search pSearch) throws IOException;

    static List<INotify> fromEnvironment()
    {
        final String gmailUser = System.getenv(GMailNotification.ENV_GMAIL_USER);
        final String gmailPw = System.getenv(GMailNotification.ENV_GMAIL_PASSWORD);
        if (gmailUser != null && gmailPw != null)
        {
            try
            {
                return List.of(new GMailNotification(gmailUser, gmailPw));
            }
            catch (Exception e)
            {
                System.out.println("Failed to create GMailNotifications:");
                e.printStackTrace(System.out);
            }
        }
        return List.of();
    }
}
