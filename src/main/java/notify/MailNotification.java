package notify;

import java.io.IOException;
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
    public void notify(final Search pSearch) throws IOException
    {
        send("Found '" + pSearch.getTitle() + "'!", "Found a '" + pSearch.getTitle() + "', see: " + pSearch.getUrl());
    }
}
