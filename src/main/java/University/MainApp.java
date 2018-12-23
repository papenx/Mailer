package University;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/MainForm.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setTitle("Chernyshov PI15V Mailer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

