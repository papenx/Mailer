package University.Controllers;

import University.Encryption.CipherUtil;
import University.Encryption.DigitalSignatureEmail;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import static University.Utilities.FileUtility.chooseDirectory;

public class EncryptionController implements Initializable {

    @FXML
    private JFXCheckBox sign_email;

    @FXML
    private JFXCheckBox crypt_email;

    @FXML
    private JFXCheckBox have_keys_rsa;

    @FXML
    private JFXPasswordField fld_password;

    private String username;
    private String content;

    private String pathToRSAPublicKey;
    private String pathToSigFile;

    public void init(String username, String content) {
        this.username = username.split("@")[0];
        this.content = content;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        have_keys_rsa.setDisable(true);
        fld_password.setDisable(true);

        sign_email.selectedProperty().addListener((observable, oldValue, newValue) -> {
            have_keys_rsa.setDisable(!newValue);
        });

        crypt_email.selectedProperty().addListener((observable, oldValue, newValue) -> {
            fld_password.setDisable(!newValue);
        });

        UnaryOperator<TextFormatter.Change> filter = change -> change.getControlNewText().matches("[0-9]?\\p{XDigit}{0,16}") ? change : null;

        TextFormatter<Integer> formatter = new TextFormatter<>(null, null, filter);
        fld_password.setTextFormatter(formatter);
    }

    public void Send(ActionEvent event) {
        if (sign_email.isSelected()) {
            if (have_keys_rsa.isSelected()) {
                pathToRSAPublicKey = DigitalSignatureEmail.generateKeysRSA(chooseDirectory(event, "Выбирите директорию для ключей RSA").getAbsolutePath(), username);
            }
            pathToSigFile = DigitalSignatureEmail.signEmailWithSaveSign(event, content, username);
        }
        if (crypt_email.isSelected()) {
            System.out.println(fld_password.getText());
            if (fld_password.getText().length() == 16) {
                content = CipherUtil.encryptEmail(event, content, fld_password.getText());
            } else {
                new Alert(Alert.AlertType.ERROR, "Пароль должен содержать 16 симоволов от 0 до 9 и от A до F").showAndWait();
            }
        }
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public String getContent() {
        return content;
    }

    public String getPathToRSAPublicKey() {
        return pathToRSAPublicKey;
    }

    public String getPathToSigFile(){
        return pathToSigFile;
    }
}
