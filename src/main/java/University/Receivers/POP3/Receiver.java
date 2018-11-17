package University.Receivers.POP3;

import University.Info.MailServers;
import University.Models.MessageHeadline;

import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver {
    private static final Logger logger = Logger.getLogger(Receiver.class.getName());

    private String username;
    private String password;
    private Properties properties;
    private Store store;

    public Receiver(String username, String password, MailServers mailServers) {
        this.username = username;
        this.password = password;

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String pathProperties = rootPath + getPathProperties(mailServers);
        properties = new Properties();
        try {
            properties.load(new FileInputStream(pathProperties));

            Session session = Session.getInstance(properties);

            store = session.getStore("pop3s");
            store.connect(username, password);
        } catch (MessagingException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
            e.printStackTrace();
        }
    }

    public List<MessageHeadline> checkMessages() {
        List<MessageHeadline> messageList = new ArrayList<>();
        try {
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);
            for (Message message : emailFolder.getMessages()) {

                StringBuilder stringBuilder = new StringBuilder();
                Address[] recipients = message.getRecipients(Message.RecipientType.TO);

                for (Address address : recipients) {
                    stringBuilder.append(decodeMailText(address.toString()) + " ");
                }

                MessageHeadline messageHeadline = new MessageHeadline(stringBuilder.toString(), message.getSubject(), message.getSentDate(), message.getMessageNumber());
                messageList.add(messageHeadline);
            }

            emailFolder.close(false);
            store.close();
        } catch (MessagingException | NullPointerException e) {
            logger.log(Level.INFO, e.getMessage());
        }

        return messageList;
    }

    private String getPathProperties(MailServers mailServers) {
        return "Properties/" +
                getNameMailServer(mailServers) +
                "/POP3/POP3.properties";
    }

    private String getNameMailServer(MailServers mailServers) {
        if (mailServers == MailServers.GMAIL)
            return "Gmail";
        else
            return "Rambler";
    }

    public String decodeMailText(String emailId) {
        String string = emailId;
        try {
            string = MimeUtility.decodeText(emailId);
        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return string;
    }

}
