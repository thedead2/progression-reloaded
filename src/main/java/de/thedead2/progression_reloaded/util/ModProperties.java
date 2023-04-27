package de.thedead2.progression_reloaded.util;

import de.thedead2.progression_reloaded.util.exceptions.CrashHandler;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;


public class ModProperties extends Properties {

    private final Path propertiesFilePath;
    private ModProperties(Path propertiesFilePath){
        this.propertiesFilePath = propertiesFilePath;
    }

    private ModProperties(){
        this(null);
    }

    public static ModProperties empty(){
        return new ModProperties();
    }

    public static ModProperties fromPath(Path path) {
        ModProperties properties = new ModProperties(path);
        try {
            properties.load(Files.newInputStream(path));
        } catch (IOException e) {
            CrashHandler.getInstance().handleException("IOException while loading ModProperties", e, Level.ERROR, true);
        }
        return properties;
    }

    public static ModProperties fromInputStream(InputStream inputStream) {
        ModProperties properties = new ModProperties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            CrashHandler.getInstance().handleException("IOException while loading ModProperties", e, Level.ERROR, true);
        }
        return properties;
    }

    @Override
    public ModProperties setProperty(String name, String value){
        super.setProperty(name, value);
        if(propertiesFilePath != null){
            try {
                this.store(Files.newOutputStream(propertiesFilePath, StandardOpenOption.WRITE), null);
                this.load(Files.newInputStream(propertiesFilePath));
            } catch (IOException e) {
                CrashHandler.getInstance().handleException("IOException while writing ModProperties", e, Level.ERROR, true);
            }
        }
        return this;
    }
}