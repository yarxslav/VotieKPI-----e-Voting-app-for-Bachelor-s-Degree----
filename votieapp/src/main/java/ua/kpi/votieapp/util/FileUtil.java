package ua.kpi.votieapp.util;

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

public class FileUtil {
    private static final Tika tika = new Tika();

    private FileUtil() {
    }

    public static String getFileType(byte[] fileContent) {
        String mimeType = tika.detect(fileContent);
        MediaType mediaType = org.apache.tika.mime.MediaType.parse(mimeType);

        if (mediaType != null) {
            return mediaType.getSubtype();
        }
        return "Unknown filetype";
    }

    public static String getContentType(String fileType) {
        return switch (fileType) {
            case "png" -> org.springframework.http.MediaType.IMAGE_PNG_VALUE;
            case "jpeg", "jpg" -> org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
            default -> org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}
