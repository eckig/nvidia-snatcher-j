package notify;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import model.Search;

public class MailNotification implements INotify
{
    private final String mHost;
    private final int mPort;
    private final String mUser;
    private final String mPassword;
    private final Address mMailFrom;
    private final Address mMailTo;

    public MailNotification(String pHost, int pPort, String pUser, String pPassword, Address pMailFrom, Address pMailTo)
    {
        mHost = pHost;
        mPort = pPort;
        mUser = pUser;
        mPassword = pPassword;
        mMailFrom = pMailFrom;
        mMailTo = pMailTo;
    }

    protected void send(final String pSubject, final String pBodyText) throws IOException
    {
        final Properties prop = new Properties();
        prop.put("mail.smtp.host", mHost);
        prop.put("mail.smtp.port", Integer.toString(mPort));
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        final Session session = Session.getInstance(prop, new Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(mUser, mPassword);
            }
        });

        try
        {
            final Message message = new MimeMessage(session);
            message.setFrom(mMailFrom);
            message.setRecipients(Message.RecipientType.TO, new Address[]{mMailTo});
            message.setSubject(pSubject);
            message.setText(pBodyText);
            Transport.send(message);
        }
        catch (MessagingException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public void notify(final Search pSearch, final String pMessage) throws IOException
    {
        send("Found '" + pSearch.product() + "'!",
                "Found possible match for '" + pSearch.product() + "':\n" + pMessage + "\n\nsee: " + pSearch.url());
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
        final MailNotification that = (MailNotification) pO;
        return mPort == that.mPort &&
                Objects.equals(mHost, that.mHost) &&
                Objects.equals(mUser, that.mUser) &&
                Objects.equals(mPassword, that.mPassword) &&
                Objects.equals(mMailFrom, that.mMailFrom) &&
                Objects.equals(mMailTo, that.mMailTo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mHost, mPort, mUser, mPassword, mMailFrom, mMailTo);
    }

    @Override
    public String toString()
    {
        return "MailNotification{" +
                "mHost='" + mHost + '\'' +
                ", mPort=" + mPort +
                ", mUser='" + mUser + '\'' +
                ", mMailFrom=" + mMailFrom +
                ", mMailTo=" + mMailTo +
                '}';
    }
}
