package com.sh.hospitaldata.utils;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.nio.charset.StandardCharsets;

public class EncryptionUtils {
    private static final String TAG = "EncryptionUtils";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 100000;

    /**
     * Encrypts patient data with a password-derived key
     */
    public static String encryptWithPassword(String data, String password) {
        try {
            // Generate random salt
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            // Derive key from password
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);

            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            // Encrypt data
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Combine salt + iv + encrypted data
            byte[] result = new byte[SALT_LENGTH + GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(salt, 0, result, 0, SALT_LENGTH);
            System.arraycopy(iv, 0, result, SALT_LENGTH, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, result, SALT_LENGTH + GCM_IV_LENGTH, encryptedData.length);

            return Base64.encodeToString(result, Base64.DEFAULT);

        } catch (Exception e) {
            Log.e(TAG, "Encryption failed", e);
            return null;
        }
    }

    /**
     * Decrypts patient data with a password-derived key
     */
    public static String decryptWithPassword(String encryptedData, String password) {
        try {
            byte[] data = Base64.decode(encryptedData, Base64.DEFAULT);

            // Extract salt, IV, and encrypted data
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[data.length - SALT_LENGTH - GCM_IV_LENGTH];

            System.arraycopy(data, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(data, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(data, SALT_LENGTH + GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            // Derive key from password
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            // Decrypt data
            byte[] decryptedData = cipher.doFinal(encrypted);
            return new String(decryptedData, StandardCharsets.UTF_8);

        } catch (Exception e) {
            Log.e(TAG, "Decryption failed", e);
            return null;
        }
    }

    /**
     * Generates a random password for sharing
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * Generates a secure hash for integrity checking
     */
    public static String generateHash(String data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Hash generation failed", e);
            return null;
        }
    }
}
