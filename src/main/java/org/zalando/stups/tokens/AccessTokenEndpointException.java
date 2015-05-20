package org.zalando.stups.tokens;

public class AccessTokenEndpointException extends IllegalStateException {
    public AccessTokenEndpointException() {
    }

    public AccessTokenEndpointException(final String s) {
        super(s);
    }

    public AccessTokenEndpointException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AccessTokenEndpointException(final Throwable cause) {
        super(cause);
    }
}
