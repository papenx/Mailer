package University.Senders.SMTP;

import University.Info.MailServers;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender {
    private static final Logger logger = Logger.getLogger(University.Senders.SMTP.Sender.class.getName());

    private String username;
    private String password;
    private Properties properties;

    /**
     * Конструктор для сохранения почты, пароля и загрузки настроек
     *
     * @param username почта пользователя от выбранного почтового сервера
     * @param password пароль пользователя от выбранного почтового сервера
     * @param tls использовать протокол TLS, иначе SSL
     * @param mailServers почтовый сервис
     * */
    public Sender(String username, String password, boolean tls, MailServers mailServers) {
        this.username = username;
        this.password = password;

        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String pathProperties = rootPath + getPathProperties(tls, mailServers);
        properties = new Properties();
        try {
            properties.load(new FileInputStream(pathProperties));
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public void sendMessage(String subject, String content, String fromEmail, String toEmail) {
        Session session = getSession();

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=UTF-8");

            Transport.send(message);

        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    public void sendMessageWithAttachments(String subject, String content, String fromEmail, String toEmail, List<File> files) {
        Session session = getSession();

        try {
            Message message = setMessage(session, subject, fromEmail, toEmail);

            BodyPart bodyPart = new MimeBodyPart();
            Multipart multipart = new MimeMultipart("mixed");

            bodyPart.setContent(content, "text/html; charset=UTF-8");
            multipart.addBodyPart(bodyPart);

            for (File file : files) {
                if (file.exists())
                    addAttachment(multipart, file);
            }
            message.setContent(multipart);

            Transport.send(message);
        } catch (MessagingException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    private Session getSession() {
        return Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private void addAttachment(Multipart multipart, File fileName) throws MessagingException {
        DataSource source = new FileDataSource(fileName);
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName.getName());
        multipart.addBodyPart(messageBodyPart);
    }

    private Message setMessage(Session session, String subject, String fromEmail, String toEmail) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        return message;
    }

    private String getPathProperties(boolean tls, MailServers mailServers){
        return "Properties/" +
                getNameMailServer(mailServers) +
                "/SMTP/" +
                isTLS(tls) +
                ".properties";
    }

    private String isTLS(boolean tls){
        return tls ? "TLS" : "SSL";
    }

    private String getNameMailServer(MailServers mailServers){
        if(mailServers == MailServers.GMAIL)
            return "Gmail";
        else
            return "Rambler";
    }
}
