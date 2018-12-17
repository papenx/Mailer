package University.MStor;

import University.Enums.FolderType;
import University.Models.MessageHeadline;

import javax.mail.*;
import javax.mail.search.MessageNumberTerm;
import javax.mail.search.SearchTerm;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MStorUtility {
    private static final Logger logger = Logger.getLogger(MStorUtility.class.getName());

    private String path;
    private String folderName;
    private String folderInboxName;
    private Properties properties;

    public MStorUtility(String path, String folderUserName, FolderType folderInboxName) {
        this.path = path;
        this.folderName = folderUserName;
        this.folderInboxName = folderInboxName.name();
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String pathProperties = rootPath + "Properties/MStore/Mstor.properties";
        properties = new Properties();
        try {
            properties.load(new FileInputStream(pathProperties));
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public List<MessageHeadline> getLocalMail() {
        List<MessageHeadline> messageList = new ArrayList<>();

        try {
            Session session = Session.getInstance(properties);

            Store store = session.getStore(new URLName("mstor:Mailbox/" + path + folderName));
            store.connect();

            Folder localEmailFolder = store.getDefaultFolder().getFolder(folderInboxName);
            localEmailFolder.open(Folder.READ_ONLY);

            Message[] messages = localEmailFolder.getMessages();

            for (Message message : messages) messageList.add(new MessageHeadline(message));

        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
        }

        return messageList;
    }

    public void createMbox(Message[] messages) {
        try {

            Session session1 = Session.getInstance(properties);

            Store store = session1.getStore(new URLName("mstor:Mailbox/" + path + folderName));
            store.connect();

            Folder folder = store.getDefaultFolder().getFolder(folderInboxName);

            if (!folder.exists()) {
                folder.create(Folder.READ_ONLY);
            }
            folder.appendMessages(messages);
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public Message getMessage(MessageHeadline message) {
        try {
            Session session = Session.getInstance(properties);

            Store store = session.getStore(new URLName("mstor:Mailbox/" + path + folderName));
            store.connect();

            Folder localEmailFolder = store.getDefaultFolder().getFolder(folderInboxName);
            localEmailFolder.open(Folder.READ_ONLY);

            SearchTerm searchTerm = new MessageNumberTerm(message.getMessageNum());
            Message[] msg = localEmailFolder.search(searchTerm);

            return msg[0];
        } catch (MessagingException | NullPointerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }
    }
}
