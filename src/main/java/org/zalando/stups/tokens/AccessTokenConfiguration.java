package org.zalando.stups.tokens;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AccessTokenConfiguration {
    private final Object tokenId;
    private final AccessTokensBuilder accessTokensBuilder;

    private final Set<Object> scopes = new HashSet<Object>();

    private boolean locked = false;

    AccessTokenConfiguration(final Object tokenId, final AccessTokensBuilder accessTokensBuilder) {
        this.tokenId = tokenId;
        this.accessTokensBuilder = accessTokensBuilder;
    }
    private void checkLock() {
        if (locked) {
            throw new IllegalStateException("scope configuration already done");
        }
    }

    private void checkNotNull(final String name, final Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    public AccessTokenConfiguration addScope(final Object scope) {
        checkLock();
        checkNotNull("scope", scope);
        scopes.add(scope);
        return this;
    }

    public AccessTokenConfiguration addScopes(final Collection<Object> scopes) {
        checkLock();
        checkNotNull("scopes", scopes);
        this.scopes.addAll(scopes);
        return this;
    }

    Object getTokenId() {
        return tokenId;
    }

    Set<Object> getScopes() {
        return Collections.unmodifiableSet(scopes);
    }

    public AccessTokensBuilder done() {
        locked = true;
        return accessTokensBuilder;
    }
}
