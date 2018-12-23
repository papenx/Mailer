package University.Models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.Date;

import static University.Utilities.MailUtility.decodeMailText;
import static University.Utilities.MailUtility.decodeRecepitntsText;

public class MessageHeadline {
    private StringProperty from;
    private StringProperty to;
    private StringProperty subject;
    private ObjectProperty<Date> date;
    private int messageNum;

    public MessageHeadline(String from, String subject, Date date, int messageNum) {
        this.from = new SimpleStringProperty(from);
        this.subject = new SimpleStringProperty(subject);
        this.date = new SimpleObjectProperty<>(date);
        this.messageNum = messageNum;
    }

    public MessageHeadline(Message message) throws MessagingException {
        String recipients = decodeRecepitntsText(message.getRecipients(Message.RecipientType.TO));
        String fromWhom = decodeRecepitntsText(message.getFrom());
        String subj = decodeMailText(message.getSubject());
        from = new SimpleStringProperty(fromWhom);
        to = new SimpleStringProperty(recipients);
        subject = new SimpleStringProperty(subj);
        date = new SimpleObjectProperty<>(message.getSentDate());
        messageNum = message.getMessageNumber();
    }

    public String getFrom() {
        return from.get();
    }

    public StringProperty fromProperty() {
        return from;
    }

    public StringProperty toProperty() {
        return to;
    }

    public void setFrom(String from) {
        this.from.set(from);
    }

    public String getSubject() {
        return subject.get();
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    public Date getDate() {
        return date.get();
    }

    public ObjectProperty<Date> dateProperty() {
        return date;
    }

    public void setDate(Date date) {
        this.date.set(date);
    }

    public int getMessageNum() {
        return messageNum;
    }

    public void setMessageNum(int messageNum) {
        this.messageNum = messageNum;
    }
}
