package University.Utilities;

import University.Enums.FolderType;
import University.Enums.MailServers;
import University.IMAP.Receiver;
import com.sun.mail.imap.IMAPFolder;
import javafx.collections.ObservableList;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static University.Enums.FolderType.*;
import static University.Info.MailInfo.GMAIL_DOMEN;
import static University.Info.MailInfo.YANDEX_DOMAINS;

public class MailUtility {
    private static final Logger logger = Logger.getLogger(Receiver.class.getName());
    private static boolean textIsHtml;


    public static boolean checkInternetConnect(){
        try {
            URL url = new URL("https://www.google.com/");
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static String decodeMailText(String text) {
        String decodeText = text;
        if (text == null)
            return "";
        try {
            decodeText = MimeUtility.decodeText(text);
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return decodeText;
    }

    public static String decodeRecepitntsText(Address[] addresses) {
        if (addresses != null)
            return Arrays.stream(addresses).map(address -> decodeMailText(address.toString()) + " ").collect(Collectors.joining());
        else
            return "";
    }

    public static IMAPFolder getLocalisedFolder(Store store, String mailFolder) {
        try {
            Folder[] folders = store.getDefaultFolder().list("*");
            for (Folder folder : folders) {
                IMAPFolder imapFolder = (IMAPFolder) folder;
                for (String attribute : imapFolder.getAttributes())
                    if (mailFolder.equals(attribute)) return imapFolder;
            }
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return null;
    }

    public static String getNameMailServer(MailServers mailServers) {
        if (mailServers == MailServers.YANDEX)
            return "Yandex";
        else if (mailServers == MailServers.GMAIL)
            return "Gmail";
        else
            return "Rambler";
    }

    public static IMAPFolder getFolder(Store store, MailServers mailServers, FolderType folderType) throws MessagingException {
        if (mailServers.equals(MailServers.RAMBLER)) {
            if (folderType.equals(INBOX))
                return (IMAPFolder) store.getFolder("INBOX");
            else if (folderType.equals(SENT))
                return (IMAPFolder) store.getFolder("SentBox");
            else if (folderType.equals(SPAM))
                return (IMAPFolder) store.getFolder("Spam");
            else if (folderType.equals(DRAFT))
                return (IMAPFolder) store.getFolder("DraftBox");
            else
                return (IMAPFolder) store.getFolder("Trash");
        } else {
            if (folderType.equals(INBOX))
                return (IMAPFolder) store.getFolder("INBOX");
            else if (folderType.equals(SENT))
                return getLocalisedFolder(store, "\\Sent");
            else if (folderType.equals(SPAM))
                return getLocalisedFolder(store, "\\Junk");
            else if (folderType.equals(DRAFT))
                return getLocalisedFolder(store, "\\Drafts");
            else
                return getLocalisedFolder(store, "\\Trash");
        }
    }

    public static String getTextFromMessage(Message message, ObservableList<String> list) throws MessagingException, IOException {
        textIsHtml = false;
        String result = "";
        if (message.isMimeType("text/plain"))
            result = "<pre>" + message.getContent().toString() + "</pre>";
        else if (message.isMimeType("text/html"))
            result = message.getContent().toString();
        else if (message.isMimeType("multipart/alternative") || message.isMimeType("multipart/*"))
            result = getTextFromMimeMultipart((MimeMultipart) message.getContent(), list);
        return result;
    }

    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart, ObservableList<String> list) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        String resultPlain = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            String disposition = bodyPart.getDisposition();
            if (disposition != null && (disposition.equals(BodyPart.ATTACHMENT))) {
                list.add(bodyPart.getDataHandler().getName());
            } else {
                if (bodyPart.isMimeType("text/html")) {
                    textIsHtml = true;
                    String html = (String) bodyPart.getContent();
//                    result.append(org.jsoup.Jsoup.parse(html).text()).append("\n");
                    result.append((String) bodyPart.getContent());
                } else if (bodyPart.getContent() instanceof MimeMultipart) {
                    result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent(), list));
                } else if (bodyPart.isMimeType("text/plain")) {
//                    result.append("<pre>").append(bodyPart.getContent()).append("\n").append("</pre>");
                    resultPlain = "<pre>" + bodyPart.getContent() + "\n" + "</pre>";
                }
            }
        }

        if (!textIsHtml) {
            result.append(resultPlain);
        }
        return result.toString();
    }

    public static MailServers checkMailServers(String username) {
        String[] text = username.split("@");
        for (String gmailDoman : GMAIL_DOMEN) {
            if (gmailDoman.equals(text[1]))
                return MailServers.GMAIL;
            else if (Arrays.asList(YANDEX_DOMAINS).contains(text[1])) {
                return MailServers.YANDEX;
            }
        }
        return MailServers.RAMBLER;
    }
}
