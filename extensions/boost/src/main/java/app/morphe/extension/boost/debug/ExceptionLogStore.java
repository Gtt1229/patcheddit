/*
 * Copyright 2026 wchill.
 *
 * See the included NOTICE file for GPLv3 section 7 terms.
 */

package app.morphe.extension.boost.debug;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Pattern;

public final class ExceptionLogStore {
    public static final int MAX_FILE_BYTES = 4 * 1024 * 1024;
    static final int MAX_ENTRY_CHARACTERS = 16 * 1024;

    private static final Pattern CREDENTIAL_LINE = Pattern.compile(
            "(?i)(\\b(?:access[ _-]?token|refresh[ _-]?token|client[ _-]?(?:id|secret)"
                    + "|password|api[ _-]?key|morphe_boost_imgbb_api_key|authorization)"
                    + "[\\\\\"']*\\s*[:=]\\s*)[^\\r\\n]*"
    );
    private static final Pattern BEARER_LINE =
            Pattern.compile("(?i)(\\bbearer[ \\t]+)[^\\r\\n]*");

    private final File directory;
    private final File current;
    private final File previous;

    public ExceptionLogStore(File directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
        current = new File(directory, "exceptions.log");
        previous = new File(directory, "exceptions.previous.log");
    }

    public synchronized void append(Throwable error, String context) throws IOException {
        LimitedWriter output = new LimitedWriter();
        PrintWriter writer = new PrintWriter(output);
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        timestamp.setTimeZone(TimeZone.getTimeZone("UTC"));
        writer.println(timestamp.format(new Date()));
        writer.println("Thread: " + Thread.currentThread().getName());
        writer.println("Context: " + context);
        if (error == null) writer.println("Boost reported an error without a throwable.");
        else error.printStackTrace(writer);
        writer.flush();
        String text = CREDENTIAL_LINE.matcher(output.text).replaceAll("$1[redacted]");
        text = BEARER_LINE.matcher(text).replaceAll("$1[redacted]");
        if (output.truncated) text += "\n[Exception entry truncated]\n";
        byte[] entry = (text + "\n").getBytes(StandardCharsets.UTF_8);
        if (entry.length > MAX_FILE_BYTES) throw new IOException("Exception entry exceeds the log size limit.");

        if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory()) {
            throw new IOException("Could not create the exception log directory.");
        }
        if (current.length() + entry.length > MAX_FILE_BYTES) {
            delete(previous);
            if (current.exists() && !current.renameTo(previous)) {
                throw new IOException("Could not rotate the exception log.");
            }
        }
        try (FileOutputStream stream = new FileOutputStream(current, true)) {
            stream.write(entry);
        }
    }

    public synchronized String read() throws IOException {
        requireDirectory();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        read(previous, output);
        read(current, output);
        return output.toString(StandardCharsets.UTF_8.name());
    }

    private static void read(File file, ByteArrayOutputStream output) throws IOException {
        if (!file.exists()) return;
        int total = 0;
        try (FileInputStream stream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) {
                total += count;
                if (total > MAX_FILE_BYTES) throw new IOException("Saved exception log exceeds its size limit.");
                output.write(buffer, 0, count);
            }
        }
    }

    public synchronized void clear() throws IOException {
        requireDirectory();
        delete(previous);
        delete(current);
    }

    private static void delete(File file) throws IOException {
        if (file.exists() && !file.delete()) throw new IOException("Could not remove a saved exception log.");
    }

    private void requireDirectory() throws IOException {
        if (directory.exists() && !directory.isDirectory()) {
            throw new IOException("The exception log path is not a directory.");
        }
    }

    private static final class LimitedWriter extends Writer {
        final StringBuilder text = new StringBuilder();
        boolean truncated;

        @Override
        public void write(char[] buffer, int offset, int length) {
            int count = Math.min(length, MAX_ENTRY_CHARACTERS - text.length());
            text.append(buffer, offset, count);
            truncated |= count != length;
        }

        @Override
        public void write(String value, int offset, int length) {
            int count = Math.min(length, MAX_ENTRY_CHARACTERS - text.length());
            text.append(value, offset, offset + count);
            truncated |= count != length;
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    }
}
