package University.Controllers;

import University.Info.MailServers;
import University.Info.MailServiceFeatures;
import University.Models.FileInfo;
import University.Models.User;
import University.Senders.SMTP.Sender;
import University.Services.FileUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static University.Services.FileUtility.getMultiFiles;
import static University.Services.MailUtility.checkMailServers;

public class SenderController implements Initializable {
    @FXML
    private HTMLEditor content;

    @FXML
    private ListView<FileInfo> listFiles;

    @FXML
    private TextField to_whom;

    @FXML
    private TextField subject_message;

    @FXML
    private Label lbl_files_info;

    private long size;

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private ObservableList<FileInfo> observableFileList = FXCollections.observableArrayList();

    private Sender sender;
    private String from;

    public void init(User user){
        from = user.getUsername();
        sender = new Sender(from, user.getPassword(), true, checkMailServers(from));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        size = 0L;
        listFiles.setItems(observableFileList);
        listFiles.setOrientation(Orientation.VERTICAL);
        listFiles.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listFiles.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                observableFileList.remove(listFiles.getSelectionModel().getSelectedItem());
                setFilesSize();
            }
        });
    }

    public void toSentAction(ActionEvent actionEvent) {
        String[] emails = to_whom.getText().replaceAll("\\s", "").split(",");
        List<String> validateEmails = new ArrayList<>();
        for (String email : emails) {
            if (validate(email))
                validateEmails.add(email);
        }

        if (validateEmails.size() != 0 && !content.getHtmlText().equals("") && !subject_message.getText().equals("")
                && content.getHtmlText().getBytes().length <= MailServiceFeatures.MAX_LETTER_SIZE_BYTES) {
            if (listFiles.getItems().size() != 0)
                validateEmails.forEach(validateEmail -> sender.sendMessageWithAttachments(subject_message.getText(), content.getHtmlText(), validateEmail, from, observableFileList));
            else
                validateEmails.forEach(validateEmail -> sender.sendMessage(subject_message.getText(), content.getHtmlText(), validateEmail, from));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.close();
        }
    }

    public void cancelAction(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }

    public void addFiles(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        observableFileList.addAll(deleteDuplicateFiles(stage));
        setFilesSize();
    }

    private List<FileInfo> deleteDuplicateFiles(Stage stage) {
        //Удаляем дубликаты
        List<FileInfo> list = getMultiFiles(stage);
        List<FileInfo> listFiltered = new ArrayList<>();
        list.removeAll(observableFileList);

        //Удаляем файлы, если макс размер превышен
        long size_temp = size;
        for (FileInfo fileInfo : list) {
            if (size_temp + fileInfo.getSize() <= MailServiceFeatures.MAX_FILES_SIZE_BYTES) {
                size_temp += fileInfo.getSize();
                listFiltered.add(fileInfo);
            }
        }

        return listFiltered;
    }

    private void setFilesSize() {
        size = observableFileList.stream().mapToLong(FileInfo::getSize).sum();
        lbl_files_info.setText(String.format("Кол-во файлов : %s Размер : %s", observableFileList.size(), FileUtility.sizeFormatter(size)));
    }

    private static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

}
