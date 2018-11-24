package University;

import University.Info.FolderType;
import University.Info.MailServers;
import University.LocalStoreProvider.MStorUtility;
import University.Receivers.IMAP.Receiver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
//    public static void main(String... args) {
//        University.Senders.TLS.Sender senderTSL = new University.Senders.TLS.Sender("rodion-belovitskiy@rambler.ru", "rodionbelovitskiy");
//        Sender senderSSl = new Sender("rodion-belovitskiy@rambler.ru", "rodionbelovitskiy", false, MailServers.RAMBLER);
//
//        senderTSL.send("Это я", "TLS text", "rodion-belovitskiy@rambler.ru", "rodion-belovitskiy@rambler.ru");
//        senderSSl.sendMessage("По поводу работы",
//                "Завтра явится на работу в 8:00 С Уважение администрация Сайта google.com",
//                "rodion-belovitskiy@rambler.ru", "rodion-belovitskiy@rambler.ru");
//        List<File> files = new ArrayList<>();
//
//        files.add(new File("C:/Rodion/Projects/MailClientKurs/src/main/resources/Properties/Rambler/MailServers/SSL.properties"));
//        files.add(new File("C:/Rodion/Projects/MailClientKurs/src/main/resources/Properties/Rambler/MailServers/TLS.properties"));
//        files.add(new File("C:/Rodion/Projects/MailClientKurs/src/main/resources/FXML/MainForm.fxml"));
//
//        senderSSl.sendMessageWithAttachments("По поводу работы",
//                "Завтра явится на работу в 8:00 С Уважение администрация Сайта google.com",
//                "rodion-belovitskiy@rambler.ru", "rodion-belovitskiy@rambler.ru", files);
//        Sender senderTSL = new Sender("majorkik.tm@gmail.com", "rodion97king16");
//        University.Senders.MailServers.Sender senderSSl = new University.Senders.MailServers.Sender("majorkik.tm@gmail.com", "rodion97king16");

//        senderTSL.sendMessage("Это я", "TLS text", "majorkik.tm@gmail.com", "majorkik.tm@gmail.com");
//        senderSSl.sendMessage("Случайный sll", "Случайный текст сообщения ssl", "majorkik.tm@gmail.com", "rodion-belovitskiy@rambler.ru,majorkik.tm@gmail.com");
//        Receiver receiver = new Receiver();
//
//        try {
//            receiver.getMessage("rodion-belovitskiy@rambler.ru", "rodionbelovitskiy");
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//        launch(args);
//    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//        Receiver receiver1 = new Receiver("rodion-belovitskiy@rambler.ru", "rodionbelovitskiy", MailServers.RAMBLER);
//        receiver1.checkMessages(FolderType.INBOX);
//        Receiver receiver2 = new Receiver("majorkik.tm@gmail.com", "rodion97king16", MailServers.GMAIL);
//        receiver2.checkMessages();

//        MStorUtility mStorUtility = new MStorUtility();

        Parent root = FXMLLoader.load(getClass().getResource("/FXML/MainForm.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setTitle("Mail client Belovitskiy Rodion Pi15-a");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

