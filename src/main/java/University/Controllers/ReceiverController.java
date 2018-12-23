package University.Controllers;

import University.Encryption.CipherUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static University.Encryption.DigitalSignatureEmail.verifyEmail;
import static University.Utilities.MailUtility.decodeMailText;
import static University.Utilities.MailUtility.decodeRecepitntsText;
import static University.Utilities.MailUtility.getTextFromMessage;
import static University.Controllers.EncryptionController.checkUserDir;
import static University.Info.MailInfo.*;

public class ReceiverController implements Initializable {
    private static final Logger logger = Logger.getLogger(ReceiverController.class.getName());

    @FXML
    private WebView webview;

    @FXML
    private TextArea contentMail;

    @FXML
    private TextField subjectMail;

    @FXML
    private TextField fromWhom;

    @FXML
    private ListView<String> listFiles;

    private Message message;

    private String separator = File.separator;

    private String downloadFolderPath = "Downloads" + separator;

    private String content;

    private ObservableList<String> observableFileList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        listFiles.setItems(observableFileList);
        listFiles.setOrientation(Orientation.VERTICAL);
        listFiles.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listFiles.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                downloadSelectedFile();
            }
        });
    }

    void init(Message message) {
        this.message = message;
        try {
            String subject = decodeMailText(message.getSubject());
            this.content = decodeMailText(getTextFromMessage(message, observableFileList)).replaceAll("\\r\\n", "");
            subjectMail.setText(subject);

            WebEngine webEngine = webview.getEngine();
            webEngine.loadContent(content);

            if (message.getFolder().getName().equals("INBOX")) {
                String from = decodeRecepitntsText(message.getFrom());
                if (from.contains("<") && from.contains(">")) {
                    from = from.substring(from.indexOf("<") + 1, from.indexOf(">"));
                }
                fromWhom.setText(from);
                checkUserDir(from.split("@")[0], globalCurrentUser);
            } else {
                String from = decodeRecepitntsText(message.getRecipients(Message.RecipientType.TO));
                fromWhom.setText(from);
                checkUserDir(globalCurrentUser, from.split("@")[0]);
            }

        } catch (MessagingException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }

    }

    public void closeWindow(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }

    private void downloadSelectedFile() {
        String selectedFileName = listFiles.getSelectionModel().getSelectedItem();
        BodyPart bodyPart = searchFile(selectedFileName, null);
        if (bodyPart != null) {
            createFile(bodyPart, selectedFileName);
        }
    }

    private BodyPart searchFile(String selectedFileName, String ext) {
        try {
            boolean extEnabled = ext != null;
            if (message.isMimeType("multipart/*")) {
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                int count = mimeMultipart.getCount();
                for (int i = 0; i < count; i++) {
                    BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                    String disposition = bodyPart.getDisposition();
                    if (extEnabled) {
                        if (disposition != null && (disposition.toLowerCase().equals(BodyPart.ATTACHMENT))){
                            if (bodyPart.getFileName().endsWith(ext)) {
                                return bodyPart;
                            }
                        }
                    } else {
                        if (disposition != null && (disposition.toLowerCase().equals(BodyPart.ATTACHMENT)) &&
                                bodyPart.getFileName().equals(selectedFileName)) {
                            return bodyPart;
                        }
                    }
                }
            }
        } catch (MessagingException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
        return null;
    }

    private void createFile(BodyPart bodyPart, String selectedFileName) {
        try {
            FileOutputStream output = new FileOutputStream(downloadFolderPath + selectedFileName);

            InputStream input = bodyPart.getInputStream();

            byte[] buffer = new byte[4096];

            int byteRead;

            while ((byteRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, byteRead);
            }
            output.close();
        } catch (MessagingException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }


    public void verifySignatureFromEmail(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Проверка ЭЦП");
        alert.setHeaderText(null);
        BodyPart bodyPart = searchFile(null, SIGNATURE_EXT);
        alert.setContentText(verifyEmail(event, content, bodyPart) ? "ЭЦП верна" : "ЭЦП подделана");

        alert.showAndWait();
    }

    public void decryptEmail(ActionEvent event) {
        WebEngine webEngine = webview.getEngine();
        String decryptedText = CipherUtil.decryptEmail(event, content);
        webEngine.loadContent(decryptedText);
        content = decryptedText;
    }
}
