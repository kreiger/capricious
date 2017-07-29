package com.linuxgods.kreiger.capricious.twitch.chat.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class SimpleStdErrAndFileLogger implements Consumer<String> {
    private final PrintWriter logWriter;
    private boolean logToStdErr = true;

    public SimpleStdErrAndFileLogger(Path logDirectory, String namePrefix) {
        try {
            this.logWriter = createLogFile(Files.createDirectories(logDirectory), namePrefix);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private PrintWriter createLogFile(Path logDirectory, String namePrefix) throws IOException {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            LocalDateTime now = LocalDateTime.now();
            Path logPath = logDirectory
                    .resolve(namePrefix + "-" + now.format(dateTimeFormatter) + ".log");

            return new PrintWriter(Files.newBufferedWriter(logPath, StandardCharsets.UTF_8), true);
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
