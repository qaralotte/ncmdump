package io.qaralotte.ncmdump;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

public class Utils {

    //AES-ECB decrypt
    public static byte[] aes_ecb_decrypt(byte[] src, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE ,keySpec);
        return cipher.doFinal(src);
    }

    //RC4-KSA build sbox
    public static byte[] rc4_ksa_build(byte[] key) {
        byte[] S = new byte[256];
        for (int i = 0; i <= 0xFF; i++) {
            S[i] = (byte) i;
        }
        int j = 0;
        for (int i = 0; i <= 0xFF; i++) {
            j = (j + S[i] + key[i % key.length]) & 0xFF;
            byte _swap = S[i];
            S[i] = S[j];
            S[j] = _swap;
        }
        return S;
    }

    //base64 decrypt
    public static byte[] base64_decrypt(byte[] src) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(src);
    }

    //pack stream.read(byte[]);
    public static void read(FileInputStream fis, byte[] b) throws Exception {
        if (fis.read(b) == -1) throw new Exception("READ BYTE ERROR!");
    }

    //pack stream.skip(long);
    public static void skip(FileInputStream fis, long n) throws Exception {
        if (fis.skip(n) < n) throw new Exception("SKIP BYTE ERROR!");
    }

    //pack stream.write(byte[]);
    public static void write(File dest, byte[] data) throws Exception {
        if (dest.exists()) {
            if (!dest.delete()) throw new Exception();
        }
        FileOutputStream fos = new FileOutputStream(dest);
        fos.write(data);
        System.out.println("write music file successfully");
        fos.flush();
        fos.close();
    }
}
