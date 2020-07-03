package com.luck.picture.lib.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/08/12
 *     desc  : utils about image
 * </pre>
 */
public final class ImageUtils {

    private ImageUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Return whether it is a image according to the file name.
     *
     * @param file The file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isImage(final File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        return isImage(file.getPath());
    }

    /**
     * Return whether it is a image according to the file name.
     *
     * @param filePath The path of file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isImage(final String filePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            return options.outWidth > 0 && options.outHeight > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Return the type of image.
     *
     * @param filePath The path of file.
     * @return the type of image
     */
    public static ImageType getImageType(final String filePath) {
        return getImageType(UtilsBridge.getFileByPath(filePath));
    }


    /**
     * Return the type of image.
     *
     * @param file The file.
     * @return the type of image
     */
    public static ImageType getImageType(final File file) {
        if (file == null) return null;
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            ImageType type = getImageType(is);
            if (type != null) {
                return type;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static ImageType getImageType(final InputStream is) {
        if (is == null) return null;
        try {
            byte[] bytes = new byte[12];
            return is.read(bytes) != -1 ? getImageType(bytes) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ImageType getImageType(final byte[] bytes) {
        String type = UtilsBridge.bytes2HexString(bytes,true).toUpperCase();
        if (type.contains("FFD8FF")) {
            return ImageType.TYPE_JPG;
        } else if (type.contains("89504E47")) {
            return ImageType.TYPE_PNG;
        } else if (type.contains("47494638")) {
            return ImageType.TYPE_GIF;
        } else if (type.contains("49492A00") || type.contains("4D4D002A")) {
            return ImageType.TYPE_TIFF;
        } else if (type.contains("424D")) {
            return ImageType.TYPE_BMP;
        } else if (type.startsWith("52494646") && type.endsWith("57454250")) {//524946461c57000057454250-12个字节
            return ImageType.TYPE_WEBP;
        } else if (type.contains("00000100") || type.contains("00000200")) {
            return ImageType.TYPE_ICO;
        } else {
            return ImageType.TYPE_UNKNOWN;
        }
    }

    private static boolean isJPEG(final byte[] b) {
        return b.length >= 2
                && (b[0] == (byte) 0xFF) && (b[1] == (byte) 0xD8);
    }

    private static boolean isGIF(final byte[] b) {
        return b.length >= 6
                && b[0] == 'G' && b[1] == 'I'
                && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private static boolean isPNG(final byte[] b) {
        return b.length >= 8
                && (b[0] == (byte) 137 && b[1] == (byte) 80
                && b[2] == (byte) 78 && b[3] == (byte) 71
                && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    private static boolean isBMP(final byte[] b) {
        return b.length >= 2
                && (b[0] == 0x42) && (b[1] == 0x4d);
    }

    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }


    public enum ImageType {
        TYPE_JPG("jpg"),

        TYPE_PNG("png"),

        TYPE_GIF("gif"),

        TYPE_TIFF("tiff"),

        TYPE_BMP("bmp"),

        TYPE_WEBP("webp"),

        TYPE_ICO("ico"),

        TYPE_UNKNOWN("unknown");

        String value;

        ImageType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
