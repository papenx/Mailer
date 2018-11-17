package University.Receivers.IMAP;

import University.Info.FolderType;
import University.Info.MailServers;
import University.Models.MessageHeadline;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPSSLStore;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.MessageNumberTerm;
import javax.mail.search.SearchTerm;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Receiver {
    private static final Logger logger = Logger.getLogger(Receiver.class.getName());

    private String username;
    private String password;
    private MailServers mailServers;
    private Properties properties;
    private IMAPFolder folder;
    private IMAPSSLStore store;

    public Receiver(String username, String password, MailServers mailServers) {
        this.username = username;
        this.password = password;
        this.mailServers = mailServers;
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String pathProperties = rootPath + getPathProperties();
        properties = new Properties();
        try {
            properties.load(new FileInputStream(pathProperties));

            storeOpen();
        } catch (MessagingException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public List<MessageHeadline> checkMessages(FolderType folderType) {
        List<MessageHeadline> messageList = new ArrayList<>();
        try {
            for (Message message : folder.getMessages()) {
                String recipientsArray = "";
                Address[] recipients = message.getRecipients(Message.RecipientType.TO);

                if (recipients != null)
                    recipientsArray = Arrays.stream(recipients).map(address -> decodeMailText(address.toString()) + " ").collect(Collectors.joining());

                MessageHeadline messageHeadline = new MessageHeadline(recipientsArray, message.getSubject(), message.getSentDate(), message.getMessageNumber());
                messageList.add(messageHeadline);
            }
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
            e.printStackTrace();
        }

        System.out.print(messageList.size());

        return messageList;
    }

    public Message getMessage(FolderType folderType, MessageHeadline message) {
        try {
            SearchTerm searchTerm = new MessageNumberTerm(message.getMessageNum());
            Message[] msg = folder.search(searchTerm);

            return msg[0];
        } catch (MessagingException | NullPointerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }
    }

    public String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain"))
            result = message.getContent().toString();
        else if (message.isMimeType("multipart/*"))
            result = getTextFromMimeMultipart((MimeMultipart) message.getContent());
        return result;
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            String disposition = bodyPart.getDisposition();
            if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {
                result.append("Файл : ").append(bodyPart.getDataHandler().getName()).append("\n");;
            }else {
                if (bodyPart.isMimeType("text/plain")) {
                    result.append(bodyPart.getContent()).append("\n");
                    break;
                } else if (bodyPart.isMimeType("text/html")) {
                    String html = (String) bodyPart.getContent();
                    result.append(org.jsoup.Jsoup.parse(html).text()).append("\n");;
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
                }
            }
        }
        return result.toString();
    }

    private IMAPFolder getFolder(FolderType folderType) throws MessagingException {
        if (folderType.equals(FolderType.INBOX)) {
            return (IMAPFolder) store.getFolder("INBOX");
        } else if (folderType.equals(FolderType.SENTBOX)) {
            if (mailServers.equals(MailServers.RAMBLER)) {
                return (IMAPFolder) store.getFolder("SentBox");
            } else {
                return getLocalisedFolder("\\Sent");
            }
        } else {
            if (mailServers.equals(MailServers.RAMBLER))
                return (IMAPFolder) store.getFolder("Spam");
            else
                return getLocalisedFolder("\\Junk");
        }
    }

    private IMAPFolder getLocalisedFolder(String mailFolder) throws MessagingException {
        Folder[] folders = store.getDefaultFolder().list("*");
        for (Folder folder : folders) {
            IMAPFolder imapFolder = (IMAPFolder) folder;
            for (String attribute : imapFolder.getAttributes())
                if (mailFolder.equals(attribute)) return imapFolder;
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

    public void openFolder(FolderType folderType) throws MessagingException {
        if (folder != null && folder.isOpen())
            folder.close(false);

        folder = getFolder(folderType);
        folder.open(Folder.READ_WRITE);
    }

    public void closeFolder() throws MessagingException {
        if (folder.isOpen())
            folder.close(false);
    }

    public void storeOpen() throws MessagingException {
        if (store == null || !store.isConnected()) {
            Session session = Session.getInstance(properties);
            store = (IMAPSSLStore) session.getStore("imaps");
            store.connect(username, password);
        }
    }

    public void storeClose() throws MessagingException {
        if (store.isConnected())
            store.close();
    }
}
