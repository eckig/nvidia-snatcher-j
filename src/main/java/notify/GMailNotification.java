package notify;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;

public class GMailNotification extends MailNotification
{
    public static final String ENV_GMAIL_USER = "GMAIL_USER";
    public static final String ENV_GMAIL_PASSWORD = "GMAIL_PASSWORD";

    public GMailNotification(String pUser, String pPassword) throws AddressException, IOException
    {
        super("smtp.gmail.com", 587, pUser, pPassword, new InternetAddress(pUser), new InternetAddress(pUser));
        send("nvidia-snatcher-j Started","nvidia-snatcher-j started working.");
    }
}
