package io.qaralotte.ncmdump;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Dump {

    private File ncm_f;
    private FileInputStream ncm_fis;
    private static final String MAGIC = "CTENFDAM";
    private static final byte[] CORE_KEY = { 0x68, 0x7A, 0x48, 0x52, 0x41, 0x6D, 0x73, 0x6F, 0x35, 0x6B, 0x49, 0x6E, 0x62, 0x61, 0x78, 0x57 };
    private static final byte[] META_KEY = { 0x23, 0x31, 0x34, 0x6C, 0x6A, 0x6B, 0x5F, 0x21, 0x5C, 0x5D, 0x26, 0x30, 0x55, 0x3C, 0x27, 0x28 };

    public Dump(File f) throws Exception {
        this.ncm_f = f;
        this.ncm_fis = new FileInputStream(f);
    }

    public void execute() throws Exception {
        if (assert_magic()) System.out.println("correct magic");
        else throw new Exception("INCORRECT MAGIC!");
        byte[] sbox = build_sbox();
        byte[] meta_data = read_meta_data();
        int crc32 = read_crc32();
        byte[] album_image = read_album_image();
        byte[] music_data = read_music_data(sbox);

        MetaData meta = MetaData.read_from_json(meta_data);
        File output_music = new File(ncm_f.getParent(), meta.musicName + "." + meta.format);
        Utils.write(output_music, music_data);
        //TODO add id3 tag;
        System.out.println("done!");
        System.out.println("output file path: " + output_music.getAbsolutePath());
    }

    private boolean assert_magic() throws Exception {
        byte[] ncm_magic = new byte[8];
        Utils.read(ncm_fis, ncm_magic);
        Utils.skip(ncm_fis, 2L);
        return Arrays.equals(ncm_magic, MAGIC.getBytes());
    }

    private byte[] build_sbox() throws Exception {
        int key_length = read_byte_length();
        byte[] key_data = new byte[key_length];
        Utils.read(ncm_fis, key_data);
        for (int i = 0; i < key_data.length; ++i) key_data[i] ^= 0x64;
        System.out.println("read key data successfully");
        byte[] decrypt_data_row = Utils.aes_ecb_decrypt(key_data, CORE_KEY);
        byte[] decrypt_data = Arrays.copyOfRange(decrypt_data_row, 17, decrypt_data_row.length);
        System.out.println("aes ecb decrypt successfully");
        byte[] sbox = Utils.rc4_ksa_build(decrypt_data);
        System.out.println("rc4 ksa build sbox successfully");
        return sbox;
    }

    private int read_byte_length() throws Exception {
        byte[] length_r = new byte[4];
        Utils.read(ncm_fis, length_r);
        return ByteBuffer.wrap(length_r).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get();
    }

    private byte[] read_meta_data() throws Exception {
        int meta_data_length = read_byte_length();
        byte[] meta_data_r = new byte[meta_data_length];
        Utils.read(ncm_fis, meta_data_r);
        for (int i = 0; i < meta_data_r.length; ++i) meta_data_r[i] ^= 0x63;
        byte[] meta_data_b64_aes = Arrays.copyOfRange(meta_data_r, 22, meta_data_r.length);
        byte[] meta_data = Utils.aes_ecb_decrypt(Utils.base64_decrypt(meta_data_b64_aes), META_KEY);
        System.out.println("read meta data successfully");
        System.out.println("meta data: ");
        System.out.println("  " + new String(meta_data));
        return Arrays.copyOfRange(meta_data, 6, meta_data.length);
    }

    private int read_crc32() throws Exception {
        int crc32 = read_byte_length();
        System.out.println("read crc32 successfully");
        return crc32;
    }

    private byte[] read_album_image() throws Exception {
        Utils.skip(ncm_fis, 5L);
        int img_length = read_byte_length();
        byte[] img = new byte[img_length];
        Utils.read(ncm_fis, img);
        System.out.println("read album image successfully");
        return img;
    }

    private byte[] read_music_data(byte[] sbox) throws Exception {
        byte[] music_data = new byte[(int) ncm_f.length()];
        Utils.read(ncm_fis, music_data);
        byte[] K = new byte[sbox.length];
        for (int i = 0; i <= 0xFF; i++) {
            K[i] = sbox[(sbox[i] + sbox[(i + sbox[i]) & 0xFF]) & 0xFF];
        }

        for (int j = 0; j < music_data.length; j++) {
            byte cursor = K[(j + 1) % K.length];
            music_data[j] ^= cursor;
        }
        return music_data;
    }

}
