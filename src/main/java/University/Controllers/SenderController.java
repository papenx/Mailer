package University.Controllers;

import University.Models.FileInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SenderController implements Initializable {
    @FXML
    private TextArea message_area;

    @FXML
    private ListView<FileInfo> list_files;

    @FXML
    private TextField to_whom;

    @FXML
    private TextField subject_message;

    @FXML
    private Label lbl_files_info;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void toSentAction(ActionEvent actionEvent) {

    }

    public void cancelAction(ActionEvent actionEvent) {

    }
}
