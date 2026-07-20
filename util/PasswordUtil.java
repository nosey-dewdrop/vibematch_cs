package util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/*
 * Handles password hashing. We never store the real password. Instead we make a
 * random salt for each user, then store a slow key-derivation of it.
 *
 * We use PBKDF2 (built into the JDK, no extra library). It runs the hash tens
 * of thousands of times on purpose, which makes guessing passwords with a
 * cracker enormously slower than a plain SHA-256 would be.
 *
 * Older accounts were created with plain SHA-256. verify() still accepts those
 * so nobody gets locked out, and AuthService upgrades them to PBKDF2 the next
 * time they log in successfully.
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    // how many PBKDF2 rounds. higher = slower to crack, also slightly slower to
    // log in. baked into the stored hash so we can raise it later without
    // breaking old hashes.
    private static final int ITERATIONS = 120000;
    private static final int KEY_BITS = 256;
    private static final String PBKDF2_PREFIX = "pbkdf2$";

    // make a new random salt, returned as a base64 string
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // hash the password together with the salt, using PBKDF2. the result carries
    // its own scheme + iteration count so verify() knows how to check it.
    public static String hash(String password, String salt) {
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        byte[] derived = pbkdf2(password, saltBytes, ITERATIONS);
        return PBKDF2_PREFIX + ITERATIONS + "$" + Base64.getEncoder().encodeToString(derived);
    }

    // check a typed password against the stored hash. handles both the new
    // PBKDF2 format and the old raw SHA-256 one.
    public static boolean verify(String password, String salt, String expectedHash) {
        if (expectedHash == null) {
            return false;
        }
        if (expectedHash.startsWith(PBKDF2_PREFIX)) {
            String[] parts = expectedHash.split("\\$");
            // parts: ["pbkdf2", iterations, base64hash]
            if (parts.length != 3) {
                return false;
            }
            int iterations;
            try {
                iterations = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return false;
            }
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            byte[] derived = pbkdf2(password, saltBytes, iterations);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            return constantTimeEquals(derived, expected);
        }
        // legacy SHA-256 account
        return constantTimeEquals(legacySha256(password, salt), expectedHash.getBytes());
    }

    // true if a stored hash is still on the old scheme and should be upgraded
    public static boolean needsUpgrade(String storedHash) {
        return storedHash == null || !storedHash.startsWith(PBKDF2_PREFIX);
    }

    // ---- internals ----

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("hashing failed", e);
        }
    }

    // the exact hashing the old PasswordUtil used, kept only so legacy accounts
    // can still log in (and then get upgraded).
    private static byte[] legacySha256(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((salt + password).getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(digest).getBytes();
        } catch (Exception e) {
            throw new RuntimeException("hashing failed", e);
        }
    }

    // compare without leaking timing info about where the mismatch is
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    private PasswordUtil() {
    }
}
