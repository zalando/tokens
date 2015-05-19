package org.zalando.stups.tokens;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class AccessTokensBuilder {
    private final URI accessTokenUri;

    private ClientCredentialsProvider clientCredentialsProvider;
    private UserCredentialsProvider userCredentialsProvider;
    private int refreshPercentLeft = 40;
    private int warnPercentLeft = 20;

    private final Set<AccessTokenConfiguration> accessTokenConfigurations = new HashSet<AccessTokenConfiguration>();

    AccessTokensBuilder(final URI accessTokenUri) {
        this.accessTokenUri = accessTokenUri;

        // TODO set default client credentials provider with JsonFileBackedClientCredentialsProvider and System.getenv(CREDENTIALS_DIR)
        // TODO set default user credentials provider with JsonFileBackedUserCredentialsProvider and System.getenv(CREDENTIALS_DIR)
    }

    public AccessTokensBuilder usingClientCredentialsProvider(final ClientCredentialsProvider clientCredentialsProvider) {
        this.clientCredentialsProvider = clientCredentialsProvider;
        return this;
    }

    public AccessTokensBuilder usingUserCredentialsProvider(final UserCredentialsProvider userCredentialsProvider) {
        this.userCredentialsProvider = userCredentialsProvider;
        return this;
    }

    public AccessTokensBuilder refreshPercentLeft(final int refreshPercentLeft) {
        this.refreshPercentLeft = refreshPercentLeft;
        return this;
    }

    public AccessTokensBuilder warnPercentLeft(final int warnPercentLeft) {
        this.warnPercentLeft = warnPercentLeft;
        return this;
    }

    public AccessTokenConfiguration manageToken(final Object tokenId) {
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
        return accessTokenConfigurations;
    }

    public AccessTokens start() {
        if (accessTokenConfigurations.size() == 0) {
            throw new IllegalArgumentException("no scopes defined");
        }
        return new AccessTokenRefresher(this);
    }
}
