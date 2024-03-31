package di.dilogin.minecraft.controller;

import java.security.MessageDigest;

/**
 * This class provides methods for encrypting data.
 */
public class EncryptionController {

    /**
     * Encrypts the given string using the SHA-256 algorithm.
     *
     * @param ip The string to encrypt.
     * @return The encrypted string as a hexadecimal string.
     */
    public static String encrypt(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(string.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
