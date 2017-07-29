package com.linuxgods.kreiger.util;

import net.harawata.appdirs.AppDirsFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public class ConfigurationPropertiesFile implements Configuration {
    private final Path file;

    private Supplier<Properties> properties = new Supplier<Properties>() {
        private Properties properties;

        @Override
        public Properties get() {
            if (null == properties) {
                try {
                    this.properties = new Properties();
                    properties.load(Files.newInputStream(file));
                } catch (NoSuchFileException ignored) {
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return properties;
        }
    };

    public String getString(Key<String> key) {
        return Optional.ofNullable(properties.get().getProperty(key.getName()))
                .orElseGet(key::getDefault);
    }

    public void setString(Key name, String value) {
        properties.get().setProperty(name.getName(), value);
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            OutputStream out = Files.newOutputStream(file);
            properties.get().store(out, "");
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ConfigurationPropertiesFile(Path file) {
        this.file = file;
    }

    public static ConfigurationPropertiesFile of(String name) {
        Path userConfigDir = getConfigDirectory(name);
        Path file = userConfigDir.resolve(name + ".properties");
        return new ConfigurationPropertiesFile(file);
    }

    private static Path getConfigDirectory(String name) {
        return Paths.get(AppDirsFactory.getInstance().getUserConfigDir(name, null, null, true));
    }

}
