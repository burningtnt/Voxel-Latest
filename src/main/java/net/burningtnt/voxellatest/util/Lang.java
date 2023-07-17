package net.burningtnt.voxellatest.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Lang {
    private Lang() {
    }

    public static String getFileType(String filename) {
        String[] l = filename.split("\\.");
        if (l.length == 0) {
            return "";
        }
        return l[l.length - 1];
    }

    public static void delete(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        if (Files.isDirectory(path)) {
            for (Path sub : Files.list(path).toList()) {
                delete(sub);
            }
        }

        Files.delete(path);
    }

    public static byte[] readAllBytesAndClose(InputStream inputStream) throws IOException {
        byte[] data = inputStream.readAllBytes();
        inputStream.close();
        return data;
    }
}
