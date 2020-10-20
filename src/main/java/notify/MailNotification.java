package notify;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import model.Search;

public class MailNotification implements INotify
{
    @Override
    public void notify(final Search pSearch)
    {
        //TODO read from environment
        final String username = "username@gmail.com";
        final String password = "password";

        final Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        final Session session = Session.getInstance(prop, new Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(username, password);
            }
        });

        try
        {
            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
            message.setSubject("Found '" + pSearch.getTitle() + "'!");
            message.setText("Found a '" + pSearch.getTitle() + "', see: " + pSearch.getUrl());
            Transport.send(message);
        }
        catch (MessagingException e)
        {
            e.printStackTrace();
        }
    }
}
