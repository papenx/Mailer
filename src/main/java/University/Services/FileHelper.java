package University.Services;

import University.Models.FileInfo;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

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
}
