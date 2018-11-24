package University.Controllers;

import University.Info.FolderType;
import University.Info.MailServers;
import University.Models.MessageHeadline;
import University.Models.User;
import University.Receivers.IMAP.Receiver;
import University.Settings.SettingsApp;
import com.jfoenix.controls.JFXSpinner;
import com.sun.mail.imap.IMAPMessage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.mail.Message;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import static University.Info.FolderType.*;
import static University.Services.MailUtility.checkMailServers;

public class MainController implements Initializable {
    @FXML
    private Button inbox;

    @FXML
    private Button sentbox;

    @FXML
    private Button spambox;

    @FXML
    private Button draftbox;

    @FXML
    private Button trashbox;

    @FXML
    private Label lbl_curr_user;

    @FXML
    private JFXSpinner spinner;

    @FXML
    private Circle statusInternetShape;

    @FXML
    private ListView<User> usersList;

    @FXML
    private TableView<MessageHeadline> tableMessages;


    private ObservableList<MessageHeadline> messagesList = FXCollections.observableArrayList();
    private ObservableList<User> accounts = FXCollections.observableArrayList();
    private Receiver receiver;
    private MailServers currentMailServer;
    private FolderType currentFolderType;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setInternetIndicator();

        usersList.setItems(accounts);
        tableMessages.setItems(messagesList);

        currentFolderType = INBOX;

        createTable(tableMessages);
        tableMessages.setRowFactory(tv -> {
            TableRow<MessageHeadline> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) viewViewMessageForm(event, row.getItem());
            });
            return row;
        });

        usersList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                setSelectedUser();
            }
        });
        setCurrentButtonFolder();
    }

    private void setSelectedUser() {
        currentUser = usersList.getSelectionModel().getSelectedItem();
        lbl_curr_user.setText(currentUser.getUsername());
        closeReceiver();
        setReceiver();
        setNewMessages();
    }

    private void setUser() {
        if (accounts.size() > 0) {
            currentUser = accounts.get(accounts.size() - 1);
            lbl_curr_user.setText(currentUser.getUsername());
            closeReceiver();
            setReceiver();
            setNewMessages();
        } else {
            currentUser = null;
            lbl_curr_user.setText("");
            closeReceiver();
            messagesList.clear();
            receiver = null;
            currentMailServer = null;
        }
    }

    @FXML
    void addUser(ActionEvent event) {
        spinner.setVisible(true);

        viewLoginForm(event);

        setUser();
        spinner.setVisible(false);
    }

    @FXML
    void getInboxMessages(ActionEvent event) {
        currentFolderType = INBOX;
        changeFolder();

    }

    @FXML
    void getSentBoxMessages(ActionEvent event) {
        currentFolderType = SENT;
        changeFolder();
    }

    @FXML
    void getJunkMessages(ActionEvent event) {
        currentFolderType = SPAM;
        changeFolder();
    }

    @FXML
    void getDraftMessages(ActionEvent event) {
        currentFolderType = DRAFT;
        changeFolder();
    }

    @FXML
    void getTrashMessages(ActionEvent event) {
        currentFolderType = TRASH;
        changeFolder();
    }

    @FXML
    void logoutUser(ActionEvent event) {
        if (usersList.getSelectionModel().getSelectedItem() != null) {
            accounts.remove(usersList.getSelectionModel().getSelectedItem());
            setUser();
        }
    }

    @FXML
    void sendAction(ActionEvent event) {
        if (currentUser != null) {
            viewSendForm(event);
        }
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

        TableColumn<MessageHeadline, Date> sentDateColumn = new TableColumn<>("date");
        sentDateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        sentDateColumn.setPrefWidth(250);
        sentDateColumn.setSortType(TableColumn.SortType.DESCENDING);

        table.getColumns().addAll(fromColumn, subjectColumn, sentDateColumn);
    }

    private void setInternetIndicator() {
        statusInternetShape.setFill(SettingsApp.getInstance().isOnline() ? Color.GREENYELLOW : Color.ORANGERED);
    }

    private void setReceiver() {
        try {
            setCurrentMailServer();
            receiver = new Receiver(currentUser.getUsername(), currentUser.getPassword(), currentMailServer);
            receiver.openFolder(currentFolderType);
            receiver.storeOpen();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void closeReceiver() {
        if (receiver != null) {
            receiver.storeClose();
            receiver.closeFolder();
        }
    }

    private void setCurrentMailServer() {
        currentMailServer = checkMailServers(currentUser.getUsername());
    }

    private void setWindow(Node event, Stage stage, Parent root, String title) {
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(event.getScene().getWindow());
        stage.showAndWait();
    }

    private void newConnection() {
        try {
            if (receiver != null) {
                receiver.openFolder(currentFolderType);
                receiver.storeOpen();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void setNewMessages() {
        if (messagesList != null && receiver != null) {
            messagesList.clear();
            messagesList.addAll(receiver.checkMessages());
            tableMessages.getSortOrder().add(tableMessages.getColumns().get(2));
        }
    }

    private void viewLoginForm(ActionEvent event) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/FXML/LoginForm.fxml"));
            Parent root = (Parent) loader.load();
            LoginFormController controller = loader.getController();
            controller.init(accounts);
            setWindow((Node) event.getSource(), stage, root, "Добавить аккаунт");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void viewSendForm(ActionEvent event) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/FXML/SendForm.fxml"));
            Parent root = loader.load();
            SenderController controller = loader.getController();
            controller.init(currentUser);
            setWindow((Node) event.getSource(), stage, root, "Отправить письмо");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void viewViewMessageForm(MouseEvent event, MessageHeadline msg) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/FXML/LetterViewForm.fxml"));
            Parent root = loader.load();
            ReceiverLetterController controller = loader.getController();
            Message imapMessage = receiver.getMessage(msg);
            controller.init((IMAPMessage) imapMessage);
            setWindow((Node) event.getSource(), stage, root, "Просмотр письма");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentButtonFolder() {
        inbox.setText(currentFolderType.equals(INBOX) ? "> Входящие" : "Входящие");
        sentbox.setText(currentFolderType.equals(SENT) ? "> Исходящие" : "Исходящие");
        spambox.setText(currentFolderType.equals(SPAM) ? "> Спам" : "Спам");
        draftbox.setText(currentFolderType.equals(DRAFT) ? "> Черновики" : "Черновики");
        trashbox.setText(currentFolderType.equals(TRASH) ? "> Корзина" : "Корзина");
    }

    private void changeFolder() {
        newConnection();
        setNewMessages();
        setCurrentButtonFolder();
    }
}
