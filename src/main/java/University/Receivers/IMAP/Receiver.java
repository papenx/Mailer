package University.Receivers.IMAP;

import University.Info.MailServers;
import University.Models.MessageHeadline;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPStore;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver {
    private static final Logger logger = Logger.getLogger(Receiver.class.getName());

    private String username;
    private String password;
    private Properties properties;
    private IMAPSSLStore store;
    private MailServers mailServers;

    public Receiver(String username, String password, MailServers mailServers){
        this.username = username;
        this.password = password;
        this.mailServers = mailServers;
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String pathProperties = rootPath + getPathProperties();
        properties = new Properties();
        try {
            properties.load(new FileInputStream(pathProperties));

            Session session = Session.getInstance(properties);

            store = (IMAPSSLStore) session.getStore("imaps");
            store.connect(username, password);
        } catch (MessagingException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }

    }

    public List<MessageHeadline> checkMessages(){
        List<MessageHeadline> messageList = new ArrayList<>();
        try{
            IMAPFolder emailFolder = getFolder();
            emailFolder.open(Folder.READ_WRITE);
            for(Message message : emailFolder.getMessages()){
                StringBuilder recipientsArray = new StringBuilder();
                Address[] recipients = message.getRecipients(Message.RecipientType.TO);

                for (Address address : recipients) {
                    recipientsArray.append(decodeMailText(address.toString()) + " ");
                }
                MessageHeadline messageHeadline = new MessageHeadline(recipientsArray.toString(), message.getSubject(), message.getSentDate());
                messageList.add(messageHeadline);
            }

            emailFolder.close(false);
            store.close();
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
        }

        System.out.print(messageList.size());

        return messageList;
    }

    private IMAPFolder getFolder() throws MessagingException {
        if(mailServers.equals(MailServers.RAMBLER)){
            return (IMAPFolder) store.getFolder("SentBox");
        }else{
            return getLocalisedFolder("\\Sent");
        }
    }

    private IMAPFolder getLocalisedFolder(String mailFolder) throws MessagingException {
        Folder[] folders = store.getDefaultFolder().list("*");
        for (Folder folder : folders) {
            IMAPFolder imapFolder = (IMAPFolder) folder;
            for (String attribute : imapFolder.getAttributes()) {
                if (mailFolder.equals(attribute)) {
                    return imapFolder;
                }
            }
        }
        return null;
    }

    private String getPathProperties() {
        return String.format("Properties/%s/IMAP/IMAP.properties", getNameMailServer());
    }

    private String getNameMailServer() {
        if (mailServers == MailServers.GMAIL)
            return "Gmail";
        else
            return "Rambler";
    }

    private String decodeMailText(String emailId) {
        String string = emailId;
        try {
            string = MimeUtility.decodeText(emailId);
        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
        }

        return string;
    }

}
