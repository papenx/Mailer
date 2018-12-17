package University.Controllers;

import University.Enums.FolderType;
import University.MStor.MStorUtility;
import University.Models.User;
import University.IMAP.Receiver;
import com.jfoenix.controls.JFXTextArea;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import static University.Utilities.MailUtility.checkMailServers;

public class SaverController implements Initializable {
    @FXML
    private JFXTextArea progress_area;

    private User currentUser;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void init(User user){
        currentUser = user;
    }

    public void start(ActionEvent event) {
        if(currentUser != null) {
            deleteFolder();
            IntStream.range(0, FolderType.values().length).parallel().forEachOrdered(i -> {
                new Thread(() -> {
                    progress_area.appendText(String.format("Началась синхронизация папки %s. \n", FolderType.values()[i]));
                    Receiver receiver = new Receiver(currentUser.getUsername(), currentUser.getPassword(), checkMailServers(currentUser.getUsername()));
                    receiver.openFolder(FolderType.values()[i]);
                    receiver.storeOpen();
                    new MStorUtility("Accounts/", currentUser.getUsername() + "/", FolderType.values()[i]).createMbox(receiver.getMessage());
                    progress_area.appendText(String.format("Папка %s успешно синхронизированна. \n", FolderType.values()[i]));
                    receiver.storeClose();
                    receiver.closeFolder();
                }).start();
            });
        }
    }

    private void deleteFolder(){
        try {
            FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + "/Mailbox/Accounts/" + currentUser.getUsername() + ".sbd"));
        } catch (IOException ignored) {
        }
    }
}
