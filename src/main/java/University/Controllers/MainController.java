package University.Controllers;

import University.Info.FolderType;
import University.Info.MailServers;
import University.Models.FileInfo;
import University.Models.MessageHeadline;
import University.Receivers.IMAP.Receiver;
import University.Settings.SettingsApp;
import com.sun.mail.imap.IMAPMessage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private Circle statusInternetShape;

    @FXML
    private ListView<FileInfo> usersList;

    @FXML
    private TableView<MessageHeadline> tableMessages;

    private ObservableList<MessageHeadline> messagesList = FXCollections.observableArrayList();

    private FolderType currentFolderType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentFolderType = FolderType.INBOX;
        if(SettingsApp.getInstance().isOnline())
            statusInternetShape.setFill(Color.GREENYELLOW);
        else
            statusInternetShape.setFill(Color.ORANGERED);

        Receiver receiver = new Receiver("rodion-belovitskiy@rambler.ru", "rodionbelovitskiy", MailServers.RAMBLER);
        try {
            receiver.openFolder(FolderType.INBOX);
            receiver.storeOpen();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        messagesList.addAll(receiver.checkMessages(FolderType.INBOX));
        createTable(tableMessages);
        tableMessages.setItems(messagesList);
        tableMessages.setRowFactory( tv -> {
            TableRow<MessageHeadline> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    MessageHeadline rowData = row.getItem();
                    try {
                        Stage sendStage = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                                "/FXML/LetterViewForm.fxml"));
                        Parent root = (Parent) loader.load();
                        ReceiverLetterController controller = loader.getController();
                        Message imapMessage = receiver.getMessage(FolderType.INBOX, rowData);
                        controller.init((IMAPMessage) imapMessage, receiver);
                        sendStage.setScene(new Scene(root));
                        sendStage.setTitle("Отправить письмо");
                        sendStage.initModality(Modality.WINDOW_MODAL);
                        sendStage.initOwner(((Node)event.getSource()).getScene().getWindow());
                        sendStage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return row ;
        });
        tableMessages.getItems().size();
    }

    @FXML
    void addUser(ActionEvent event) {

    }

    @FXML
    void getIncomingLetters(ActionEvent event) {

    }

    @FXML
    void getOutgoingLetters(ActionEvent event) {

    }

    @FXML
    void getSpamLetters(ActionEvent event) {

    }

    @FXML
    void logoutUser(ActionEvent event) {

    }

    @FXML
    void sendAction(ActionEvent event) throws IOException {
        Stage sendStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/SendForm.fxml"));
        sendStage.setScene(new Scene(root));
        sendStage.setTitle("Отправить письмо");
        sendStage.initModality(Modality.WINDOW_MODAL);
        sendStage.initOwner(((Node)event.getSource()).getScene().getWindow());
        sendStage.show();
    }

    @FXML
    void settingsAction(ActionEvent event) {

    }

    private void createTable(TableView table) {

        TableColumn<MessageHeadline, String> fromColumn = new TableColumn<>("from");
        fromColumn.setCellValueFactory(param -> param.getValue().fromProperty());
        fromColumn.setPrefWidth(150);

        TableColumn<MessageHeadline, String> subjectColumn = new TableColumn<>("subject");
        subjectColumn.setCellValueFactory(param -> param.getValue().subjectProperty());
        subjectColumn.setPrefWidth(250);

        TableColumn<MessageHeadline, Date> santDateColumn = new TableColumn<>("date");
        santDateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        santDateColumn.setPrefWidth(250);

        table.getColumns().addAll(fromColumn, subjectColumn, santDateColumn);
    }

}
