package org.zalando.stups.tokens;

import java.util.HashSet;
import java.util.Set;

public class AccessTokenConfiguration {
    private final Object tokenId;
    private final AccessTokensBuilder accessTokensBuilder;

    private final Set<Object> scopes = new HashSet<Object>();

    AccessTokenConfiguration(final Object tokenId, final AccessTokensBuilder accessTokensBuilder) {
        this.tokenId = tokenId;
        this.accessTokensBuilder = accessTokensBuilder;
    }

    public AccessTokenConfiguration addScope(final Object scope) {
        scopes.add(scope);
        return this;
    }

    Object getTokenId() {
        return tokenId;
    }

    Set<Object> getScopes() {
        return scopes;
    }

    public AccessTokensBuilder done() {
        return accessTokensBuilder;
    }
}
