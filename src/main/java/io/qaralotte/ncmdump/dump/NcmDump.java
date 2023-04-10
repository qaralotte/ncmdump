package io.qaralotte.ncmdump.dump;

import com.alibaba.fastjson2.JSONObject;
import io.qaralotte.ncmdump.utils.DecryptUtils;
import io.qaralotte.ncmdump.utils.ErrorUtils;
import io.qaralotte.ncmdump.utils.StreamUtils;
import io.qaralotte.ncmdump.utils.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.generic.AudioFileReader;
import org.jaudiotagger.audio.mp3.MP3FileReader;
import org.jaudiotagger.audio.mp4.Mp4FileReader;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.StandardArtwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NcmDump {

    private final File file;
    private FileInputStream inputStream;
    private String neteaseKey;

    static {

        // Disable loggers
        Logger[] pin = {
                Logger.getLogger("org.jaudiotagger")
        };

        for (Logger l : pin) {
            l.setLevel(Level.OFF);
        }
    }

    public NcmDump(File ncmFile) {

        this.file = ncmFile;

        try {
            this.inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            ErrorUtils.error("File not found", "Path: " + ncmFile.getAbsolutePath());
        }

    }

    public void execute() {

        System.out.println("- Start dumping .ncm -");

        assertMagic();
        byte[] keyData = readKeyData();
        byte[] keyBox = buildKeyBox(keyData);
        MetaData metaData = readMetaData();
        readCRC32();
        byte[] albumImageData = readAlbumImageData();
        byte[] musicData = readMusicData(keyBox);

        File musicFile = writeMusicData(metaData, musicData);

        fixId3Tags(musicFile, metaData, albumImageData);

        System.out.println("- Finish dumping .ncm -");
        System.out.println("=> Output file path: " + musicFile.getAbsolutePath());

    }

    // Verify .ncm magic is equals 'NcmKey.MAGIC'
    private void assertMagic() {
        System.out.print("Verify .ncm MAGIC => ");

        byte[] magicBytes = new byte[8];
        StreamUtils.readBytes(inputStream, magicBytes);
        StreamUtils.skipN(inputStream, 2);

        if (!Arrays.equals(magicBytes, NcmKey.MAGIC)) {
            System.out.println("=> Error: Incorrect MAGIC");
        }

        System.out.println("Correct");
    }

    private int readBytesLength() {
        byte[] keyLength = new byte[4];
        StreamUtils.readBytes(inputStream, keyLength);
        return ByteBuffer.wrap(keyLength).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get();
    }

    private byte[] buildKeyBox(byte[] keyData) {

        System.out.print("AES-ECB decrypt => ");
        byte[] decryptDataRaw = DecryptUtils.AESECBDecrypt(keyData, NcmKey.CORE_KEY);

        // remove "neteasecloudmusic"
        byte[] decryptData = Arrays.copyOfRange(decryptDataRaw, 17, decryptDataRaw.length);
        System.out.println("Success");

        System.out.print("RC4-KSA build Key Box => ");
        decryptData = DecryptUtils.RC4KSA(decryptData);
        System.out.println("Success");

        return decryptData;
    }

    private byte[] readKeyData() {
        System.out.print("Read KEY data => ");

        // Key Data Length
        int keyLength = readBytesLength();

        // Key Data
        byte[] keyData = new byte[keyLength];
        StreamUtils.readBytes(inputStream, keyData);
        for (int i = 0; i < keyData.length; ++i) {
            keyData[i] ^= 0x64;
        }

        System.out.println("Success");

        return keyData;
    }

    private MetaData readMetaData() {

        System.out.print("Read META data => ");

        int metaDataLength = readBytesLength();
        byte[] metaDataRaw = new byte[metaDataLength];

        StreamUtils.readBytes(inputStream, metaDataRaw);
        for (int i = 0; i < metaDataLength; ++i) {
            metaDataRaw[i] ^= 0x63;
        }

        neteaseKey = StringUtils.toString(metaDataRaw);

        // remove "163 key(don't modify)"
        byte[] metaDataAesBase64 = Arrays.copyOfRange(metaDataRaw, 22, metaDataRaw.length);
        byte[] metaDataAes = DecryptUtils.base64Decrypt(metaDataAesBase64);
        byte[] metaData = DecryptUtils.AESECBDecrypt(metaDataAes, NcmKey.META_KEY);

        System.out.println("Success");

        // remove "music:"
        metaData = Arrays.copyOfRange(metaData, 6, metaData.length);

        MetaData metaDataObject = new MetaData(metaData);
        System.out.println(metaDataObject);

        return metaDataObject;
    }

    private void readCRC32() {
        System.out.print("Read CRC32 => ");
        readBytesLength();
        System.out.println("Success");
    }

    private byte[] readAlbumImageData() {

        System.out.print("Read album image => ");

        StreamUtils.skipN(inputStream, 5L);
        int imgDataLength = readBytesLength();
        byte[] img = new byte[imgDataLength];
        StreamUtils.readBytes(inputStream, img);

        System.out.println("Success");
        return img;
    }

    private byte[] readMusicData(byte[] keyBox) {

        System.out.print("Read MUSIC data => ");

        byte[] musicData = new byte[(int) file.length()];
        int bytesLength = StreamUtils.readBytes(inputStream, musicData);
        musicData = Arrays.copyOf(musicData, bytesLength);

        DecryptUtils.RC4PRGA(musicData, keyBox);
        System.out.println("Success");

        return musicData;
    }

    private File writeMusicData(MetaData metaData, byte[] musicData) {
        System.out.print("Write MUSIC file => ");

        JSONObject metaDataJson = metaData.getJson();

        String artistsName = Arrays.toString(metaData.getArtistsName());

        // remove []
        artistsName = artistsName.substring(1, artistsName.length() - 1);

        String musicName = metaDataJson.getString("musicName");
        String musicFormat = metaDataJson.getString("format");

        String musicFileName = String.format("%s - %s.%s", artistsName, musicName, musicFormat);
        File musicFile = new File(file.getParent(), musicFileName);
        StreamUtils.writeBytes(musicFile, musicData);

        System.out.println("Success");
        try {
            inputStream.close();
        } catch (IOException e) {
            ErrorUtils.error("InputStream can not be closed");
        }

        return musicFile;
    }

    public void fixId3Tags(File musicFile, MetaData metaData, byte[] albumImage) {

        System.out.print("Fix ID3 Tag => ");

        try {

            String format = metaData.getJson().getString("format");
            AudioFileReader audioFileReader = null;

            if ("flac".equals(format)) {
                audioFileReader = new FlacFileReader();
            } else if ("mp3".equals(format)) {
                audioFileReader = new MP3FileReader();
            } else if ("mp4".equals(format)) {
                audioFileReader = new Mp4FileReader();
            } else {
                ErrorUtils.error("Unsupported format: " + format);
            }

            if (audioFileReader == null) return;
            AudioFile audioFile = audioFileReader.read(musicFile);
            Tag tag = audioFile.getTag();

            tag.deleteArtworkField();
            tag.setField(FieldKey.TITLE, metaData.getJson().getString("musicName"));
            for (String artistName : metaData.getArtistsName()) {
                tag.addField(FieldKey.ARTIST, artistName);
            }
            tag.setField(FieldKey.ALBUM, metaData.getJson().getString("album"));
            tag.setField(FieldKey.COMMENT, neteaseKey);

            StandardArtwork artwork = new StandardArtwork();
            artwork.setBinaryData(albumImage);
            artwork.setMimeType("image/jpeg");
            artwork.setPictureType(3);

            tag.setField(artwork);
            audioFile.setTag(tag);
            audioFile.commit();

        } catch (CannotWriteException e) {
            ErrorUtils.error("ID3 tag cannot be written in File");
        } catch (FieldDataInvalidException e) {
            ErrorUtils.error("Error field data");
        } catch (CannotReadException e) {
            ErrorUtils.error("Music file cannot read");
        } catch (TagException e) {
            ErrorUtils.error("Tag error");
        } catch (InvalidAudioFrameException e) {
            ErrorUtils.error("Invalid audio");
        } catch (ReadOnlyFileException e) {
            ErrorUtils.error("Music file readonly");
        } catch (IOException e) {
            ErrorUtils.error("Music file read failed");
        }

        System.out.println("Success");

    }


}
