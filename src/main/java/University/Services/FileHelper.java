package University.Services;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import static University.Info.MailServiceFeatures.MAX_FILES_SIZE;

public class FileHelper {

    public static List<File> getMultiFiles(double occupiedSpace, Stage primaryStage){
        FileChooser fileChooser = new FileChooser();
        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
        Iterator<File> file = files.iterator();

        double freeSpace = MAX_FILES_SIZE - occupiedSpace;

        while (file.hasNext()){
            if(file.next().exists())
                if(freeSpace - file.next().length() < 0)
                    file.remove();
                else
                    freeSpace -= file.next().length();
        }

        return files;
    }
}
