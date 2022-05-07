package io.qaralotte.ncmdump;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.mp3.MP3FileReader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.ID3v22Tag;
import org.jaudiotagger.tag.images.StandardArtwork;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Dump {

    private File ncm_f;
    private FileInputStream ncm_fis;
    private static final byte[] MAGIC = { 0x43, 0x54, 0x45, 0x4E, 0x46, 0x44, 0x41, 0x4D };
    private static final byte[] CORE_KEY = { 0x68, 0x7A, 0x48, 0x52, 0x41, 0x6D, 0x73, 0x6F, 0x35, 0x6B, 0x49, 0x6E, 0x62, 0x61, 0x78, 0x57 };
    private static final byte[] META_KEY = { 0x23, 0x31, 0x34, 0x6C, 0x6A, 0x6B, 0x5F, 0x21, 0x5C, 0x5D, 0x26, 0x30, 0x55, 0x3C, 0x27, 0x28 };
    private String netease_key;

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
        System.out.println("  crc32: " + crc32);
        byte[] album_image = read_album_image();
        byte[] music_data = read_music_data(sbox);

        MetaData meta = MetaData.read_from_json(meta_data);
        StringBuilder artists = new StringBuilder();
        for (int _index = 0; _index < meta.artist.length; ++_index) {
            if (_index != 0) artists.append(", ");
            artists.append(meta.artist[_index][0]);
        }
        File output_music = new File(ncm_f.getParent(), artists.toString() + " - " + meta.musicName + "." + meta.format);
        Utils.write(output_music, music_data);

        AudioFile audio_file;
        if (meta.format.equals("flac")) {
            FlacFileReader flac_reader = new FlacFileReader();
            audio_file = flac_reader.read(output_music);
            FlacTag flac_tag = (FlacTag) audio_file.getTag();
            fix_tag(audio_file, meta, album_image, flac_tag);
        } else if (meta.format.equals("mp3")) {
            MP3FileReader mp3_reader = new MP3FileReader();
            audio_file = mp3_reader.read(output_music);
            Tag id3_tag = audio_file.getTag();
            fix_tag(audio_file, meta, album_image, id3_tag);
        } else {
            throw new Exception("UNSUPPORT FORMAT!");
        }

        System.out.println("done!");
        System.out.println("output file path: " + output_music.getAbsolutePath());
        ncm_fis.close();
    }

    public void fix_tag(AudioFile audio, MetaData meta_data, byte[] cover, Tag tag) throws Exception {
        tag.deleteArtworkField();
        tag.setField(FieldKey.TITLE, meta_data.musicName);
        for (int i = 0; i < meta_data.artist.length; ++i) {
            tag.addField(FieldKey.ARTIST, meta_data.artist[i][0]);
        }
        tag.setField(FieldKey.ALBUM, meta_data.album);
        tag.setField(FieldKey.COMMENT, netease_key);

        StandardArtwork artwork = new StandardArtwork();
        artwork.setBinaryData(cover);
        artwork.setMimeType("image/jpeg");
        artwork.setPictureType(3);

        tag.setField(artwork);
        audio.setTag(tag);
        audio.commit();
        System.out.println("fix tag successfully");
    }

    private boolean assert_magic() throws Exception {
        byte[] ncm_magic = new byte[8];
        Utils.read(ncm_fis, ncm_magic);
        Utils.skip(ncm_fis, 2L);
        return Arrays.equals(ncm_magic, MAGIC);
    }

    private byte[] build_sbox() throws Exception {
        int key_length = read_byte_length();
        byte[] key_data = new byte[key_length];
        Utils.read(ncm_fis, key_data);
        for (int i = 0; i < key_data.length; ++i) key_data[i] ^= 0x64;
        System.out.println("read key data successfully");
        byte[] decrypt_data_row = Utils.aes_ecb_decrypt(key_data, CORE_KEY);
        byte[] decrypt_data = Arrays.copyOfRange(decrypt_data_row, 17, decrypt_data_row.length); //"neteasecloudmusic"
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
        netease_key = new String(meta_data_r, StandardCharsets.UTF_8);
        byte[] meta_data_b64_aes = Arrays.copyOfRange(meta_data_r, 22, meta_data_r.length); //"163 key(don't modify)"
        System.out.println("  " + "163 key: " + new String(meta_data_b64_aes, StandardCharsets.UTF_8));
        byte[] meta_data = Utils.aes_ecb_decrypt(Utils.base64_decrypt(meta_data_b64_aes), META_KEY);
        System.out.println("read meta data successfully");
        System.out.println("meta data: ");
        System.out.println("  " + new String(meta_data, StandardCharsets.UTF_8));
        return Arrays.copyOfRange(meta_data, 6, meta_data.length); //"music:"
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
