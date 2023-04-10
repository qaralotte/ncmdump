package io.qaralotte.ncmdump.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DecryptUtils {

    // AES-ECB decrypt
    public static byte[] AESECBDecrypt(byte[] src, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(src);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            System.out.println("Error");
            throw new RuntimeException(e);
        }
    }

    // Base64 Decrypt
    public static byte[] base64Decrypt(byte[] src) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(src);
    }

    // RC4-KSA algorithm
    public static byte[] RC4KSA(byte[] k) {
        byte[] s = new byte[256];
        for (int i = 0; i <= 255; i++) {
            s[i] = (byte) i;
        }

        int j = 0;
        for (int i = 0; i <= 255; i++) {
            j = (j + s[i] + k[i % k.length]) & 255;
            byte swap = s[i];
            s[i] = s[j];
            s[j] = swap;
        }
        return s;
    }

    // RC4-PRGA algorithm
    public static void RC4PRGA(byte[] src, byte[] s) {

        byte[] k = new byte[256];

        for (int i = 0; i <= 255; i++) {
            k[i] = s[(s[i] + s[(i + s[i]) & 255]) & 255];
        }

        for (int j = 0; j < src.length; j++) {
            src[j] ^= k[(j + 1) % 256];
        }
    }

}
