package University.Utilities;

import University.Models.FileInfo;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileUtility {

    public static List<FileInfo> getMultiFiles(Stage primaryStage){
        FileChooser fileChooser = new FileChooser();
        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
        if(files == null)
            return new ArrayList<>();
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (File file : files) {
            fileInfoList.add(new FileInfo(file));
        }
        return fileInfoList;
    }

    public static String sizeFormatter(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static File chooseFile(ActionEvent event, String description, String[] extensions) {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(description, extensions);
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().setAll(extFilter);
        return fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
    }

    public static File chooseDirectory(ActionEvent event, String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        return directoryChooser.showDialog(((Node) event.getSource()).getScene().getWindow());
    }
}
