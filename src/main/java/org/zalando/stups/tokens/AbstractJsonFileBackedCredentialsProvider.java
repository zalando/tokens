package org.zalando.stups.tokens;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public abstract class AbstractJsonFileBackedCredentialsProvider {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final File file;

    private static File getCredentialsDir() {
        final String dir = System.getenv("CREDENTIALS_DIR");
        if (dir == null) {
            throw new IllegalStateException("environment variable CREDENTIALS_DIR not set");
        }
        return new File(dir);
    }

    public AbstractJsonFileBackedCredentialsProvider(final String filename) {
        this(new File(getCredentialsDir(), filename));
    }

    public AbstractJsonFileBackedCredentialsProvider(final File file) {
        this.file = file;
    }

    protected File getFile() {
        return file;
    }

    protected  <T> T read(final Class<T> cls) {
        try {
            return OBJECT_MAPPER.readValue(file, cls);
        } catch (final Throwable e) {
            throw new CredentialsUnavailableException(e.getMessage(), e);
        }
    }
}
