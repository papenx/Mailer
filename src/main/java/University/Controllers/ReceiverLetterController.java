package University.Controllers;

import University.Receivers.IMAP.Receiver;
import com.sun.mail.imap.IMAPMessage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ReceiverLetterController implements Initializable {

    @FXML
    private TextArea contentMail;

    @FXML
    private TextField subjectMail;

    @FXML
    private TextField fromWhom;

    private IMAPMessage message;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void init(IMAPMessage message, Receiver receiver) {
        this.message = message;
        try {
            subjectMail.setText(message.getSubject());
            fromWhom.setText(Arrays.toString(message.getFrom()));
            contentMail.setText(receiver.getTextFromMessage(message));
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}
