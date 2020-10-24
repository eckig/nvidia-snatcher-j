package notify;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class GMailNotification extends MailNotification
{
    public static final String ENV_GMAIL_USER = "GMAIL_USER";
    public static final String ENV_GMAIL_PASSWORD = "GMAIL_PASSWORD";

    public GMailNotification(final String pUser, final String pPassword) throws AddressException
    {
        super("smtp.gmail.com", 587, pUser, pPassword, new InternetAddress(pUser), new InternetAddress(pUser));
    }
}
