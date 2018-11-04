package University.Settings;

import java.net.URL;
import java.net.URLConnection;

public class SettingsApp {
    private static volatile SettingsApp instance;
    private static final Object mutex = new Object();

    private SettingsApp() {}

    public static SettingsApp getInstance() {
        SettingsApp result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null)
                    instance = result = new SettingsApp();
            }
        }
        return result;
    }

    private boolean checkInternetConnectivity(){
        try {
            URL url = new URL("https://www.google.com/");
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean isOnline() {
        return checkInternetConnectivity();
    }
}
