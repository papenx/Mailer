package University.Info;

public class MailInfo {
    public static final int MAX_LETTER_SIZE_BYTES = 31_400_000;
    public static final long MAX_FILES_SIZE_BYTES = 20_800_000;
    public static final short MAX_NUMBERS_OF_RECIPIENTS = 25;
    public static final short MAX_NUM_FILES = 100;

    public static final String[] RAMBLER_DOMENS = {
            "rambler.ru",
            "lenta.ru",
            "autorambler.ru",
            "myrabler.ru",
            "ro.ru",
            "rambler.ua"
    };

    public static final String[] GMAIL_DOMEN = {"gmail.com"};

    public static final String [] YANDEX_DOMAINS = {
      "yandex.ru",
      "yandex.ua"
    };

    public static final String DES_ALG = "DES";
    public static final String AES_ALG = "AES";
    public static final String DSA_ALG = "DSA";
    public static final String RSA_ALG = "RSA";
    public static final String SHA_256_WITH_DSA = "SHA256withDSA";
    public static final String MD5_WITH_RSA = "MD5withRSA";
    public static final String SECURE_RANDOM_ALG = "SHA1PRNG";

    public static final String PROVIDER = "SUN";

    public static final String DSA_PUBLIC_KEY_EXT = ".spub";
    public static final String DSA_PRIVATE_KEY_EXT = ".spk";

    public static final String RSA_PUBLIC_KEY_EXT = ".spub";
    public static final String RSA_PRIVATE_KEY_EXT = ".spk";

    public static final String SIGNATURE_EXT = ".sig";
    public static final String PUBLIC_KEY_EXT = ".pub";
    public static final String PRIVATE_KEY_EXT = ".pk";


}
