package org.zalando.stups.tokens;

import java.net.URI;

public final class Tokens {
    private Tokens() {
    }

    public static AccessTokensBuilder createAccessTokensWithUri(final URI accessTokenUri) {
        return new AccessTokensBuilder(accessTokenUri);
    }
}
