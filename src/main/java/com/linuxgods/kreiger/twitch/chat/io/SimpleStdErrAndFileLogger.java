package com.linuxgods.kreiger.twitch.chat.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class SimpleStdErrAndFileLogger implements Consumer<String> {
    private final PrintWriter logWriter;
    private boolean logToStdErr = true;

    public SimpleStdErrAndFileLogger(String namePrefix) {
        this.logWriter = createLogFile(namePrefix);
    }

    private PrintWriter createLogFile(String namePrefix) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            LocalDateTime now = LocalDateTime.now();
            Path logPath = Paths.get(namePrefix + "-" + now.format(dateTimeFormatter) + ".log");
            return new PrintWriter(Files.newBufferedWriter(logPath, StandardCharsets.UTF_8), true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void accept(String line) {
        if (logToStdErr) {
            System.err.println(line);
        }
        logWriter.println(line);
    }

    public void setLogToStdErr(boolean logToStdErr) {
        this.logToStdErr = logToStdErr;
    }
}
