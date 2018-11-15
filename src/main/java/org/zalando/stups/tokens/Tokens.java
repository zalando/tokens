package org.zalando.stups.tokens;

public final class Tokens {

    private Tokens() {

    }

    public static Builder create() {
        return new AccessTokensBuilder();
    }

}
