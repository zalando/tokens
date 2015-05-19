package org.zalando.stups.tokens;

import java.util.Date;

public class AccessToken {
    private final String token;
    private final String type;
    private final long initialValidSeconds;
    private final Date validUntil;

    public AccessToken(final String token, final String type, final long initialValidSeconds, final Date validUntil) {
        this.token = token;
        this.type = type;
        this.initialValidSeconds = initialValidSeconds;
        this.validUntil = validUntil;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public long getInitialValidSeconds() {
        return initialValidSeconds;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - validUntil.getTime()) >= 0;
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", validUntil=" + validUntil +
                '}';
    }
}
