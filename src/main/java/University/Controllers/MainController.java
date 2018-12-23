package University.Controllers;

import University.Encryption.DigitalSignatureEmail;
import University.Encryption.RSA;
import University.Enums.FolderType;
import University.Enums.MailServers;
import University.IMAP.Receiver;
import University.MStor.MStorUtility;
import University.Models.MessageHeadline;
import University.Models.User;
import University.Utilities.MailUtility;
import com.jfoenix.controls.JFXSpinner;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import javax.mail.Message;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static University.Enums.FolderType.*;
import static University.Info.MailInfo.globalCurrentUser;
import static University.Utilities.MailUtility.checkMailServers;

public class MainController implements Initializable {
    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    @FXML
    private Button syncButton;

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

    @FXML
    private Button addUserButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private Button sentButton;

    private ObservableList<MessageHeadline> messagesList = FXCollections.observableArrayList();
    private ObservableList<User> accounts = FXCollections.observableArrayList();
    private Receiver receiver;
    private MailServers currentMailServer;
    private FolderType currentFolderType;
    private User currentUser;

    private boolean is_online;

    public  Message imapMessage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkInternetConnection();

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
        globalCurrentUser = currentUser.getUsername().split("@")[0];
        if (is_online) {
            closeReceiver();
            setReceiver();
            setNewMessages();
        } else {
            setOfflineNewMessage();
        }
    }

    private void setUser() {
        if (accounts.size() > 0) {
            currentUser = accounts.get(accounts.size() - 1);
            lbl_curr_user.setText(currentUser.getUsername());
            globalCurrentUser = currentUser.getUsername().split("@")[0];
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
        if (is_online) {
            if (usersList.getSelectionModel().getSelectedItem() != null) {
                accounts.remove(usersList.getSelectionModel().getSelectedItem());
                setUser();
            }
        } else {
            try {
                FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/Mailbox/Accounts/" + currentUser.getUsername() + ".sbd"));
            } catch (IOException ignored) {
            }
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
        table.setPlaceholder(new Label("Пока ещё нет сообщений"));

        TableColumn<MessageHeadline, String> fromColumn = new TableColumn<>("Отправитель");
        fromColumn.setCellValueFactory(param -> param.getValue().fromProperty());
        fromColumn.setPrefWidth(200);

        TableColumn<MessageHeadline, String> toColumn = new TableColumn<>("Получатель");
        toColumn.setCellValueFactory(param -> param.getValue().toProperty());
        toColumn.setPrefWidth(200);

        TableColumn<MessageHeadline, String> subjectColumn = new TableColumn<>("Тема");
        subjectColumn.setCellValueFactory(param -> param.getValue().subjectProperty());
        subjectColumn.setPrefWidth(175);

        TableColumn<MessageHeadline, Date> sentDateColumn = new TableColumn<>("Дата");
        sentDateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        sentDateColumn.setPrefWidth(175);

        sentDateColumn.setSortType(TableColumn.SortType.DESCENDING);

        table.getColumns().addAll(fromColumn, toColumn, subjectColumn, sentDateColumn);
    }

    private void checkInternetConnection() {
        is_online = MailUtility.checkInternetConnect();
        statusInternetShape.setFill(is_online ? Color.LIGHTGREEN : Color.GREY);
        if (!is_online) {
            appWithoutInternet();
            sentButton.setDisable(!is_online);
            addUserButton.setDisable(!is_online);
            syncButton.setDisable(!is_online);
        }
    }

    private void setReceiver() {
        setCurrentMailServer();
        try {
            receiver = new Receiver(currentUser.getUsername(), currentUser.getPassword(), currentMailServer);
            receiver.openFolder(currentFolderType);
            receiver.storeOpen();
        } catch (NullPointerException e) {
            logger.log(Level.INFO, e.getMessage());
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
            logger.log(Level.INFO, e.getMessage());
        }
    }

    private void setNewMessages() {
        if (messagesList != null && receiver != null) {
            messagesList.clear();
            messagesList.addAll(receiver.checkMessages());
            tableMessages.getSortOrder().add(tableMessages.getColumns().get(3));
        }
    }

    private void setOfflineNewMessage() {
        if (messagesList != null && accounts.size() != 0) {
            messagesList.clear();
            messagesList.addAll(new MStorUtility("Accounts/", currentUser.getUsername() + ".sbd/", currentFolderType).getLocalMail());
            tableMessages.getSortOrder().add(tableMessages.getColumns().get(3));
        }
    }

    private void viewLoginForm(ActionEvent event) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/FXML/LoginForm.fxml"));
            Parent root = (Parent) loader.load();
            LoginController controller = loader.getController();
            controller.init(accounts);
            setWindow((Node) event.getSource(), stage, root, "Добавить учётную запись");
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
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
            setWindow((Node) event.getSource(), stage, root, "Написать письмо");
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    private void viewViewMessageForm(MouseEvent event, MessageHeadline msg) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/FXML/ReceiveForm.fxml"));
            Parent root = loader.load();
            ReceiverController controller = loader.getController();

            if (is_online)
                imapMessage = receiver.getMessage(msg);
            else
                imapMessage = new MStorUtility("Accounts/", currentUser.getUsername() + ".sbd/", currentFolderType).getMessage(msg);
            controller.init(imapMessage);
            setWindow((Node) event.getSource(), stage, root, "Чтение письма");
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }

    private void setCurrentButtonFolder() {
        inbox.setText(currentFolderType.equals(INBOX) ? "> Входящие" : "Входящие");
        sentbox.setText(currentFolderType.equals(SENT) ? "> Отправленные" : "Отправленные");
        spambox.setText(currentFolderType.equals(SPAM) ? "> Спам" : "Спам");
        draftbox.setText(currentFolderType.equals(DRAFT) ? "> Черновики" : "Черновики");
        trashbox.setText(currentFolderType.equals(TRASH) ? "> Корзина" : "Корзина");
    }

    public void generateKeysRSA(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите директорию для ключей для шифрования RSA");
        File selectedDirectory = directoryChooser.showDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedDirectory != null) RSA.generateKeysRSA(selectedDirectory.getAbsolutePath(), currentUser.getUsername());
    }

    public void generateKeysRSA2(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите директорию для ключей для цифровой подписи RSA");
        File selectedDirectory = directoryChooser.showDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedDirectory != null) {
            String username;
            if (currentUser != null)
                username = currentUser.getUsername().split("@")[0];
            else
                username = "RSA KEYS";
            DigitalSignatureEmail.generateKeysRSA(selectedDirectory.getAbsolutePath(), username);
        }

    }

    private void changeFolder() {
        if (is_online) {
            newConnection();
            setNewMessages();
        } else {
            setOfflineNewMessage();
        }
        setCurrentButtonFolder();
    }

    public void sync(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/FXML/SaverForm.fxml"));
        Parent root = loader.load();
        SaverController controller = loader.getController();
        controller.init(currentUser);
        setWindow((Node) event.getSource(), stage, root, "Синхронизация учётной записи " + currentUser.getUsername());
    }

    private void appWithoutInternet() {
        Stream.of(
                Objects.requireNonNull(new File(System.getProperty("user.dir") + "/Mailbox/Accounts/")
                        .listFiles(File::isDirectory))
        ).forEach(
                (File f) -> accounts.add(
                        new User(
                                f.getName().replaceAll(".sbd", ""),
                                "",
                                checkMailServers(f.getName().replaceAll(".sbd", ""))
                        )));
    }
}
