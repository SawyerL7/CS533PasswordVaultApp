package com.smd.passwordvault.helpers;

import java.security.Key;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/*
 * PBKDF2 salted password hashing.
 *
 * http://crackstation.net/hashing-security.htm
 * https://stackoverflow.com/questions/23561104/how-to-encrypt-and-decrypt-string-with-my-passphrase-in-java-pc-not-mobile-plat
 */
public class EncryptionUtil {
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    private static final String SECRET_KEY = "R$HGSWDKEYPVSMD$"; // Should be 16 bytes
    private static final String PWD_HASH_SALT = "FG$SDKK";

    // The following constants may be changed without breaking existing hashes.
    private static final int HASH_BYTES = 24;
    private static final int PBKDF2_ITERATIONS = 1000;


    public static String generateRandomPassword(int pwdSize)
    {
        String allowedChars = "0123456789qwertyuiopasdfghjklzxcvbnm!@#$%^&*";
        Random rd = new Random();
        StringBuilder sb = new StringBuilder(pwdSize);

        for(int i = 0; i < pwdSize; ++i){
            sb.append(allowedChars.charAt(rd.nextInt(allowedChars.length())));
        }

        return sb.toString();
    }


    /**
     * creates a one way hash of the password
     * @param strPassword
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static String createPasswordHash(String strPassword)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = PWD_HASH_SALT.getBytes();
        char[] password = strPassword.toCharArray();
        // Hash the password
        byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTES);
        // convert it to hex
        return toHex(hash);
    }


    /**
     * Computes the PBKDF2 hash of a password.
     *
     * @param password   the password to hash.
     * @param salt       the salt
     * @param iterations the iteration count (slowness factor)
     * @param bytes      the length of the hash to compute in bytes
     * @return the PBDKF2 hash of the password
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param array the byte array to convert
     * @return a length*2 character string encoding the byte array
     */
    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }

    /**
     * Converts a string of hexadecimal characters into a byte array.
     *
     * @param hex the hex string
     * @return the hex string decoded into a byte array
     */
    private static byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }


    public static String encryptPassword(String origPwd) {
        String encPwd = null;
        try {
            // Create key and cipher
            Key aesKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(origPwd.getBytes());
            encPwd = toHex(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encPwd;

    }

    public static String decryptPassword(String encPwd) {
        String decPwd = null;
        try {
            // Create key and cipher
            Key aesKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            byte[] encrypted = fromHex(encPwd);
            // decrypt the text
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            decPwd = new String(cipher.doFinal(encrypted));
            System.err.println("========Dec Pwd:" + decPwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decPwd;
    }
}
