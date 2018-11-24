package University.LocalStoreProvider;

import University.Info.FolderType;
import University.Info.MailServers;
import University.Receivers.IMAP.Receiver;

import javax.mail.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MStorUtility {
    private static final Logger logger = Logger.getLogger(Receiver.class.getName());

    private String path;
    private String folderName;
    private Properties properties;

    public MStorUtility(String path, String folderName) {
        this.path = path;
        this.folderName = folderName;
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String pathProperties = rootPath + "Properties/MStore/Mstor.properties";
        properties = new Properties();
        try {
            properties.load(new FileInputStream(pathProperties));
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public Message[] getLocalMail() {
        Message[] messages = null;

        try {
            Session session = Session.getInstance(properties);

            Store store = session.getStore(new URLName("mstor:Mailbox/" + path));
            store.connect();

            Folder localEmailFolder = store.getDefaultFolder().getFolder(folderName);
            localEmailFolder.open(Folder.READ_ONLY);
            messages = localEmailFolder.getMessages();

        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
        }

        return messages;
    }

    public void createMbox(Message[] messages){
        try {

                Session session1 = Session.getInstance(properties);

                //The mailbox is stored in the Mailbox.sbd directory
                Store store = session1.getStore(new URLName("mstor:Mailbox/" + path));
                store.connect();

                Folder folder1 = store.getDefaultFolder().getFolder(folderName);

                if (!folder1.exists()) {
                    folder1.create(Folder.HOLDS_MESSAGES);
                }
                folder1.appendMessages(messages);
            } catch (MessagingException ex) {
                ex.printStackTrace();
            }
    }
}
