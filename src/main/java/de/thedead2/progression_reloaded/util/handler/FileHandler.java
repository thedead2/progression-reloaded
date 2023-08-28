package de.thedead2.progression_reloaded.util.handler;

import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.exceptions.FileCopyException;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;


public abstract class FileHandler extends ModHelper {

    public static void checkForMainDirectories() {
        createDirectory(DIR_PATH.toFile());
        createDirectory(LEVELS_PATH.toFile());
        createDirectory(QUESTS_PATH.toFile());
    }


    public static boolean createDirectory(File directoryIn) {
        CrashHandler.getInstance().setActiveFile(directoryIn);
        if(!directoryIn.exists()) {
            if(directoryIn.mkdir()) {
                LOGGER.debug("Created directory: " + directoryIn.toPath());
                CrashHandler.getInstance().setActiveFile(null);
                return true;
            }
            else {
                CrashHandler.getInstance().handleException("Failed to create directory at: " + directoryIn.toPath(), new RuntimeException("Unable to create directory! Maybe something is blocking file access?!"), Level.FATAL);
                return false;
            }
        }
        else {
            LOGGER.debug("Found directory {} at {}", directoryIn.getName(), directoryIn.toPath());
            CrashHandler.getInstance().setActiveFile(null);
            return false;
        }
    }


    public static void readDirectory(File directory, Consumer<File> fileReader) {
        if(directory.exists()) {
            File[] folders = directory.listFiles();

            assert folders != null;
            if(Arrays.stream(folders).anyMatch(File::isFile)) {
                fileReader.accept(directory);
            }

            for(File subfolder : folders) {
                if(subfolder.isDirectory()) {
                    fileReader.accept(subfolder);

                    readSubDirectories(subfolder, fileReader);
                }
            }
        }
    }


    private static void readSubDirectories(File folderIn, Consumer<File> fileReader) {
        for(File folder : Objects.requireNonNull(folderIn.listFiles())) {
            if(folder.isDirectory()) {
                fileReader.accept(folder);
                readSubDirectories(folder, fileReader);
            }
        }
    }


    public static void copyModFiles(String pathIn, Path pathOut, String filter) throws FileCopyException {
        Path filespath = THIS_MOD_FILE().findResource(pathIn);

        try(Stream<Path> paths = Files.list(filespath)) {
            paths.filter(path -> path.toString().endsWith(filter)).forEach(path -> {
                try {
                    writeFile(Files.newInputStream(path), pathOut.resolve(path.getFileName().toString()));
                }
                catch(IOException e) {
                    FileCopyException copyException = new FileCopyException("Failed to copy mod files!");
                    copyException.addSuppressed(e);
                    throw copyException;
                }
            });
            LOGGER.debug("Copied files from directory " + MOD_ID + ":" + pathIn + " to directory {}", pathOut);
        }
        catch(IOException e) {
            FileCopyException copyException = new FileCopyException("Unable to locate directory: " + MOD_ID + ":" + pathIn);
            copyException.addSuppressed(e);
            throw copyException;
        }
    }


    public static void writeFile(InputStream inputStream, Path outputPath) throws IOException {
        CrashHandler.getInstance().setActiveFile(outputPath.toFile());
        OutputStream fileOut = Files.newOutputStream(outputPath);

        writeToFile(inputStream, fileOut);

        fileOut.close();
        CrashHandler.getInstance().setActiveFile(null);
    }


    public static void writeToFile(InputStream inputStream, OutputStream fileOut) throws IOException {
        int input;
        while((input = inputStream.read()) != -1) {
            fileOut.write(input);
        }

        inputStream.close();
    }
}
