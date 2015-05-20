package org.zalando.stups.tokens;

public class AccessTokenUnavailableException extends IllegalStateException {
    public AccessTokenUnavailableException() {
    }

    public AccessTokenUnavailableException(final String s) {
        super(s);
    }

    public AccessTokenUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AccessTokenUnavailableException(final Throwable cause) {
        super(cause);
    }
}
