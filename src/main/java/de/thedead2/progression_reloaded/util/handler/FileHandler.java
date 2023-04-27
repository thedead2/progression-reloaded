package de.thedead2.progression_reloaded.util.handler;

import de.thedead2.progression_reloaded.util.ModHelper;
import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import de.thedead2.progression_reloaded.util.exceptions.FileCopyException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class FileHandler extends ModHelper {

    private final File directory;

    public FileHandler(File directory){
        this.directory = directory;
    }

    public static void checkForMainDirectories() {
        createDirectory(DIR_PATH.toFile());
    }

    public void start() {
        if (this.directory.exists()){
            File[] folders = this.directory.listFiles();

            assert folders != null;
            if(Arrays.stream(folders).anyMatch(File::isFile)){
                this.readFiles(this.directory);
            }

            for(File subfolder : folders){
                if(subfolder.isDirectory()){
                    this.readFiles(subfolder);

                    readSubDirectories(subfolder);
                }
            }
        }
    }


    private void readSubDirectories(File folderIn){
        for(File folder: Objects.requireNonNull(folderIn.listFiles())){
            if(folder.isDirectory()){
                this.readFiles(folder);
                readSubDirectories(folder);
            }
        }
    }


    public static boolean createDirectory(File directoryIn){
        CrashHandler.getInstance().setActiveFile(directoryIn);
        if (!directoryIn.exists()) {
            if (directoryIn.mkdir()){
                LOGGER.debug("Created directory: " + directoryIn.toPath());
                CrashHandler.getInstance().setActiveFile(null);
                return true;
            }
            else {
                LOGGER.fatal("Failed to create directory at: " + directoryIn.toPath());
                throw new RuntimeException("Unable to create directory! Maybe something is blocking file access?!");
            }
        }
        else {
            LOGGER.debug("Found directory {} at {}", directoryIn.getName(), directoryIn.toPath());
            CrashHandler.getInstance().setActiveFile(null);
            return false;
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
        while ((input = inputStream.read()) != -1){
            fileOut.write(input);
        }

        inputStream.close();
    }


    public static void copyModFiles(String pathIn, Path pathOut, String filter) throws FileCopyException {
        Path filespath = THIS_MOD_FILE.findResource(pathIn);

        try (Stream<Path> paths = Files.list(filespath)) {
            paths.filter(path -> path.toString().endsWith(filter)).forEach(path -> {
                try {
                    writeFile(Files.newInputStream(path), pathOut.resolve(path.getFileName().toString()));
                }
                catch (IOException e) {
                    FileCopyException copyException = new FileCopyException("Failed to copy mod files!");
                    copyException.addSuppressed(e);
                    throw copyException;
                }
            });
            LOGGER.debug("Copied files from directory " + MOD_ID + ":" + pathIn + " to directory {}", pathOut);
        }
        catch (IOException e) {
            FileCopyException copyException = new FileCopyException("Unable to locate directory: " + MOD_ID + ":" + pathIn);
            copyException.addSuppressed(e);
            throw copyException;
        }
    }

    protected abstract void readFiles(File directory);
}
