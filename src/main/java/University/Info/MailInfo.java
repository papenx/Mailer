package University.Info;

public class MailInfo {
    public static final int MAX_LETTER_SIZE_BYTES = 31_400_000;
    public static final long MAX_FILES_SIZE_BYTES = 20_800_000;
    public static final short MAX_NUMBERS_OF_RECIPIENTS = 25;
    public static final short MAX_NUM_FILES = 100;
    public static final short MAX_USERS_VALUE = 10;

    public static final String[] GMAIL_DOMEN = {"gmail.com"};

    public static final String [] YANDEX_DOMAINS = {
            "yandex.ru",
            "yandex.ua",
            "ya.ru"
    };

    public static final String RSA_ALG = "RSA";
    public static final String SHA_256_WITH_RSA = "SHA256withRSA";


    public static final String RSA_PUBLIC_KEY_EXT = ".spub";
    public static final String RSA_PRIVATE_KEY_EXT = ".spk";

    public static final String SIGNATURE_EXT = ".sig";
    public static final String PUBLIC_KEY_EXT = ".pub";
    public static final String PRIVATE_KEY_EXT = ".pk";

    public static String pathToRSAPublicKey;
    public static String pathToRSAPrivateKey;

    public static String pathToRSAPublicKeyCipher;
    public static String pathToRSAPrivateKeyCipher;

    public static String globalCurrentUser;
    public static String globalSignFolder;


}
