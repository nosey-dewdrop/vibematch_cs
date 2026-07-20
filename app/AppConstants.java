package app;

/*
 * General app wide constants. Stuff like the app name, the sqlite file,
 * which email domains we accept and a few validation limits.
 */
public class AppConstants {

    public static final String APP_NAME = "vibematch";

    // sqlite database file, created in the project root on first run
    public static final String DB_FILE = "vibematch.db";

    // only bilkent students can sign up
    public static final String[] ALLOWED_EMAIL_DOMAINS = {
        "@bilkent.edu.tr",
        "@ug.bilkent.edu.tr"
    };

    // validation limits
    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 20;
    public static final int PASSWORD_MIN = 6;
    public static final int BIO_MAX = 200;

    // verification code is 6 digits
    public static final int VERIFICATION_CODE_LENGTH = 6;

    private AppConstants() {
    }
}
