package University.Controllers;

import University.Enums.MailServers;
import University.Models.User;
import University.SMTP.Sender;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static University.Utilities.MailUtility.checkMailServers;

public class LoginController implements Initializable {
    @FXML
    private TextField fld_username;

    @FXML
    private PasswordField fld_pass;

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private ObservableList<User> users;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void init(ObservableList<User> observableList){
        users = observableList;
    }

    public void login(ActionEvent actionEvent) {
        String username = fld_username.getText();
        if (validate(username) && checkUserName(username, users)) {
            MailServers mailServers = checkMailServers(username);
            Sender sender = new Sender(username, fld_pass.getText(), true, mailServers);
            if (sender.isConnected()) {
                users.add(new User(username, fld_pass.getText(), mailServers));
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.close();
            }
        }
    }

    private boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    private boolean checkUserName(String username, ObservableList<User> observableList) {
        return observableList.stream().parallel().noneMatch(user -> user.getUsername().equals(username));
    }
}
