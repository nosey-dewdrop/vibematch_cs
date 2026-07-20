package util;

import java.io.FileInputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*
 * UTIL -- EmailSender
 *
 * sends the verification code over real smtp (gmail by default). this is what
 * VerificationService calls instead of just printing the code to the console --
 * "Sends and checks the Bilkent email verification" (4.1) actually sends now.
 *
 * where the login comes from, in order: environment variables first
 * (VIBEMATCH_EMAIL_SENDER / VIBEMATCH_EMAIL_PASSWORD / ...), then the local
 * credentials.properties file, then a default for the smtp host/port. the point
 * of that order is the password NEVER has to sit in the repo -- the real file is
 * gitignored, and on a server youd use env vars with nothing on disk.
 *
 * if nothing is configured, or the send fails, sendVerificationCode returns false
 * and VerificationService falls back to printing the code so the app still works
 * for anyone testing without a mail account set up.
 */
public class EmailSender {

    private static final String CREDS_FILE = "credentials.properties";

    public static boolean isConfigured(){
        return notBlank(sender()) && notBlank(password());
    }

    // returns true if the mail actually went out, false if we couldnt send it
    public static boolean sendVerificationCode(String toEmail, String code){
        String sender = sender();
        String password = password();
        String host = host();
        String port = port();

        if (!notBlank(sender) || !notBlank(password)){
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // anonymous Authenticator is the shape javamail wants here, not a lambda --
        // the Session hands our sender/password back through this when it connects
        Session session = Session.getInstance(props, new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(sender, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender, "vibematch"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your vibematch verification code");
            message.setText("Hey!\n\nYour verification code is: " + code
                    + "\n\nType this in the app to finish signing up.\n\nSee you inside,\nvibematch");
            Transport.send(message);
            return true;
        } catch (Exception e){
            // dont crash the signup -- let the caller fall back to showing the code
            System.out.println("email send failed: " + e.getMessage());
            return false;
        }
    }

    // each setting: env var wins, then the properties file, then a default for the
    // smtp bits (sender/password have no default on purpose, no login means no send)

    private static String sender(){
        return pick("VIBEMATCH_EMAIL_SENDER", "email.sender", null);
    }

    private static String password(){
        return pick("VIBEMATCH_EMAIL_PASSWORD", "email.password", null);
    }

    private static String host(){
        return pick("VIBEMATCH_SMTP_HOST", "email.smtp.host", "smtp.gmail.com");
    }

    private static String port(){
        return pick("VIBEMATCH_SMTP_PORT", "email.smtp.port", "587");
    }

    private static String pick(String envKey, String fileKey, String fallback){
        String env = System.getenv(envKey);
        if (notBlank(env)){
            return env;
        }
        Properties p = loadCreds();
        if (p != null){
            String v = p.getProperty(fileKey);
            if (notBlank(v)){
                return v;
            }
        }
        return fallback;
    }

    private static Properties loadCreds(){
        FileInputStream in = null;
        try {
            in = new FileInputStream(CREDS_FILE);
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (Exception e){
            // file probably just isnt there for this person, thats fine
            return null;
        } finally {
            try {
                if (in != null){
                    in.close();
                }
            } catch (Exception ignore){
            }
        }
    }

    private static boolean notBlank(String s){
        return s != null && !s.trim().isEmpty();
    }

    private EmailSender(){
    }

}
