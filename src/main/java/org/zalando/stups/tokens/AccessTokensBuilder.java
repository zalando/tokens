package org.zalando.stups.tokens;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AccessTokensBuilder {
    private final URI accessTokenUri;

    private ClientCredentialsProvider clientCredentialsProvider = new JsonFileBackedClientCredentialsProvider();
    private UserCredentialsProvider userCredentialsProvider = new JsonFileBackedUserCredentialsProvider();
    private int refreshPercentLeft = 40;
    private int warnPercentLeft = 20;

    private final Set<AccessTokenConfiguration> accessTokenConfigurations = new HashSet<AccessTokenConfiguration>();

    private boolean locked = false;

    AccessTokensBuilder(final URI accessTokenUri) {
        this.accessTokenUri = accessTokenUri;
    }

    private void checkLock() {
        if (locked) {
            throw new IllegalStateException("configuration already done");
        }
    }

    private void checkNotNull(final String name, final Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    public AccessTokensBuilder usingClientCredentialsProvider(final ClientCredentialsProvider clientCredentialsProvider) {
        checkLock();
        checkNotNull("clientCredentialsProvider", clientCredentialsProvider);
        this.clientCredentialsProvider = clientCredentialsProvider;
        return this;
    }

    public AccessTokensBuilder usingUserCredentialsProvider(final UserCredentialsProvider userCredentialsProvider) {
        checkLock();
        checkNotNull("userCredentialsProvider", userCredentialsProvider);
        this.userCredentialsProvider = userCredentialsProvider;
        return this;
    }

    public AccessTokensBuilder refreshPercentLeft(final int refreshPercentLeft) {
        checkLock();
        this.refreshPercentLeft = refreshPercentLeft;
        return this;
    }

    public AccessTokensBuilder warnPercentLeft(final int warnPercentLeft) {
        checkLock();
        this.warnPercentLeft = warnPercentLeft;
        return this;
    }

    public AccessTokenConfiguration manageToken(final Object tokenId) {
        checkLock();
        checkNotNull("tokenId", tokenId);
        final AccessTokenConfiguration config = new AccessTokenConfiguration(tokenId, this);
        accessTokenConfigurations.add(config);
        return config;
    }

    URI getAccessTokenUri() {
        return accessTokenUri;
    }

    ClientCredentialsProvider getClientCredentialsProvider() {
        return clientCredentialsProvider;
    }

    UserCredentialsProvider getUserCredentialsProvider() {
        return userCredentialsProvider;
    }

    int getRefreshPercentLeft() {
        return refreshPercentLeft;
    }

    int getWarnPercentLeft() {
        return warnPercentLeft;
    }

    Set<AccessTokenConfiguration> getAccessTokenConfigurations() {
        return Collections.unmodifiableSet(accessTokenConfigurations);
    }

    public AccessTokens start() {
        if (accessTokenConfigurations.size() == 0) {
            throw new IllegalArgumentException("no scopes defined");
        }
        locked = true;
        return new AccessTokenRefresher(this);
    }
}
