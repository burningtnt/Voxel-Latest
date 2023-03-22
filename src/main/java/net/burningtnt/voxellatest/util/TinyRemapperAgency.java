package net.burningtnt.voxellatest.util;

import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

public class TinyRemapperAgency {
    public static void run(File remapperFile, String fromName, String toName, File fromFile, File toFile, File sourceFile) {
        LoggerManagerUtil.info(String.format(
                "Remap file from namespace \"%s\" to namespace \"%s\" with remapper \"%s\" from path \"%s\" to path \"%s\" with Tiny Remapper",
                fromName, toName, remapperFile.getAbsolutePath(), fromFile.getAbsolutePath(), toFile.getAbsolutePath()
        ));
        TinyRemapper.Builder builder = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(remapperFile.toPath(), fromName, toName))
                .ignoreFieldDesc(false)
                .withForcedPropagation(Collections.emptySet())
                .propagatePrivate(false)
                .propagateBridges(TinyRemapper.LinkedMethodPropagation.DISABLED)
                .removeFrames(false)
                .ignoreConflicts(false)
                .checkPackageAccess(false)
                .fixPackageAccess(false)
                .resolveMissing(false)
                .rebuildSourceFilenames(false)
                .skipLocalVariableMapping(false)
                .renameInvalidLocals(false)
                .invalidLvNamePattern(null)
                .threads(-1);
        TinyRemapper remapper = builder.build();

        Path[] sourcePath = sourceFile != null ? new Path[]{sourceFile.toPath()} : new Path[]{};

        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(toFile.toPath()).build()) {
            outputConsumer.addNonClassFiles(fromFile.toPath(), NonClassCopyMode.FIX_META_INF, remapper);

            remapper.readInputs(fromFile.toPath());
            remapper.readClassPath(sourcePath);

            remapper.apply(outputConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            remapper.finish();
        }
    }
}
