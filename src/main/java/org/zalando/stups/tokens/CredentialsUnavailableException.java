package org.zalando.stups.tokens;

public class CredentialsUnavailableException extends IllegalStateException {
    public CredentialsUnavailableException() {
    }

    public CredentialsUnavailableException(final String message) {
        super(message);
    }

    public CredentialsUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CredentialsUnavailableException(final Throwable cause) {
        super(cause);
    }
}
