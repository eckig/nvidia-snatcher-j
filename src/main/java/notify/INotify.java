package notify;

import model.Search;

import java.util.List;

public interface INotify
{
    void notify(final Search pSearch);

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
                e.printStackTrace();
            }
        }
        return List.of();
    }
}
