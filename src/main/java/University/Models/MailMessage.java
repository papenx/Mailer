package University.Models;

import java.util.Date;

public class MailMessage {
    private String from;
    private String subject;
    private Date date;

    public MailMessage(String from, String subject, Date date) {
        this.from = from;
        this.subject = subject;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
