package University.Controllers;

import University.Info.MailInfo;
import University.Models.FileInfo;
import University.Models.User;
import University.SMTP.Sender;
import University.Utilities.FileUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.helper.StringUtil;



import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static University.Info.MailInfo.*;
import static University.Utilities.FileUtility.getMultiFiles;
import static University.Utilities.MailUtility.checkMailServers;

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

    public void init(User user) {
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

        if (validateEmails.size() != 0 && validateEmails.size() <= MAX_NUMBERS_OF_RECIPIENTS && !content.getHtmlText().equals("") && !subject_message.getText().equals("")
                && content.getHtmlText().getBytes().length <= MailInfo.MAX_LETTER_SIZE_BYTES) {
            if (listFiles.getItems().size() != 0 && listFiles.getItems().size() <= MAX_NUM_FILES)
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
        List<FileInfo> list = getMultiFiles(stage);
        List<FileInfo> listFiltered = new ArrayList<>();
        list.removeAll(observableFileList);

        long size_temp = size;
        for (FileInfo fileInfo : list) {
            if (size_temp + fileInfo.getSize() <= MailInfo.MAX_FILES_SIZE_BYTES) {
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

    public void encAndSend(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/FXML/EncryptionForm.fxml"));
        Parent root = loader.load();
        EncryptionController controller = loader.getController();
        String to = to_whom.getText().replaceAll("\\s", "").split("@")[0];
        controller.init(from, content.getHtmlText(), to);
        stage.setScene(new Scene(root));
        stage.setTitle("Конфигурация отправки");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node) event.getSource()).getScene().getWindow());
        stage.showAndWait();

        content.setHtmlText(controller.getContent());
        System.out.println("Отправлено:" + content.getHtmlText().length());

        addToListFilesRSA(FilenameUtils.removeExtension(controller.getPathToRSAPublicKey()), RSA_PUBLIC_KEY_EXT);
        addToListFilesRSA(controller.getPathToSigFile(), SIGNATURE_EXT);

    }

    private void addToListFilesRSA(String path, String EXT) {
        if (StringUtil.isBlank(path))
            return;
        File file = new File(path + EXT);
        if (file.exists() && file.isFile())
            observableFileList.add(new FileInfo(file));
    }
}
