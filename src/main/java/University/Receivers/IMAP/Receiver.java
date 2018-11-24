package University.Receivers.IMAP;

import University.BQ.MessageConsumer;
import University.BQ.MessageProducer;
import University.BQ.ProducerChecker;
import University.Info.FolderType;
import University.Info.MailServers;
import University.Models.MessageHeadline;
import com.google.common.io.ByteStreams;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPSSLStore;
import net.fortuna.mstor.MStorFolder;
import net.fortuna.mstor.MStorStore;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageNumberTerm;
import javax.mail.search.SearchTerm;
import java.io.*;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static University.Services.MailUtility.getFolder;
import static University.Services.MailUtility.getNameMailServer;
import static org.apache.camel.util.FileUtil.createNewFile;

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
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public Message[] getMessage() {
        try {
            return folder.getMessages();
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<MessageHeadline> checkMessages() {
        List<MessageHeadline> messageList = new ArrayList<>();
        try {
            Message[] messages = folder.getMessages();
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages, profile);

            for (Message message : messages) messageList.add(new MessageHeadline(message));

        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage());
        }

        return messageList;
    }

    public Message getMessage(MessageHeadline message) {
        try {
            SearchTerm searchTerm = new MessageNumberTerm(message.getMessageNum());
            Message[] msg = folder.search(searchTerm);

            return msg[0];
        } catch (MessagingException | NullPointerException e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }
    }

    private String getPathProperties() {
        return String.format("Properties/%s/IMAP/IMAP.properties", getNameMailServer(mailServers));
    }

    public void openFolder(FolderType folderType) {
        try {
            if (folder != null && folder.isOpen())
                folder.close(false);
            folder = getFolder(store, mailServers, folderType);
            folder.open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public void closeFolder() {
        try {
            if (folder != null && folder.isOpen())
                folder.close(false);
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public void storeOpen() {
        try {
            if (store == null || !store.isConnected()) {
                Session session = Session.getInstance(properties);
                store = (IMAPSSLStore) session.getStore("imaps");
                store.connect(username, password);
            }
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public void storeClose() {
        try {
            if (store != null && store.isConnected())
                store.close();
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
