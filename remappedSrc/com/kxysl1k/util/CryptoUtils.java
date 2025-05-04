package com.kxysl1k.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

public class CryptoUtils {
    private static SecretKey aesKey;
    private static IvParameterSpec iv;

    static {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            aesKey = keyGen.generateKey();
            byte[] ivBytes = new byte[16];
            new SecureRandom().nextBytes(ivBytes);
            iv = new IvParameterSpec(ivBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, iv);
        byte[] enc = cipher.doFinal(data);
        byte[] combined = new byte[iv.getIV().length + enc.length];
        System.arraycopy(iv.getIV(), 0, combined, 0, iv.getIV().length);
        System.arraycopy(enc, 0, combined, iv.getIV().length, enc.length);
        return combined;
    }

    public static SecretKey getKey() { return aesKey; }
}