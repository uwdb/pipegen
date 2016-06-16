package org.brandonhaynes.pipegen.utilities;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class FileRestorer {
    private static final Logger log = Logger.getLogger(FileRestorer.class.getName());

    public static void restoreFiles(Path backupPath, String[] extensions) throws IOException {
        if(backupPath.toFile().exists())
            for(File file: FileUtils.listFiles(backupPath.toFile(), extensions, true)) {
                log.info("Restoring " + file);
                Files.move(file.toPath(), Paths.get(file.toString().replaceAll("^" + backupPath.toString(), "")),
                           StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
