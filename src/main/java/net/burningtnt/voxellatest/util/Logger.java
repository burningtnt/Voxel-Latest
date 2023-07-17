package net.burningtnt.voxellatest.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class Logger {
    private Logger() {
    }

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("Voxel Latest");

    public static void info(String str) {
        LOGGER.info("[Voxel Latest] {}.", str);
    }

    public static void warn(String str, Throwable e) {
        LOGGER.warn("[Voxel Latest] {}\n{}", str, ExceptionLogBuilder.build(e));
    }

    public static void warn(String str) {
        LOGGER.warn("[Voxel Latest] {}.", str);
    }

    public static void fail(String name, Throwable e) {
        LOGGER.error("[Voxel Latest] {}.\n{}", name, ExceptionLogBuilder.build(e));
    }

    public static void fail(String str) {
        LOGGER.warn("[Voxel Latest] {}.", str);
    }

    public static class ExceptionLogBuilder {
        public static String build(Throwable e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String res = stringWriter.toString();
            IOUtils.closeQuietly(stringWriter);
            IOUtils.closeQuietly(printWriter);
            return res;
        }
    }
}
