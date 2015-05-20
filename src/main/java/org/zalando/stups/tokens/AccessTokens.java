package org.zalando.stups.tokens;

public interface AccessTokens {
    String get(Object tokenId) throws AccessTokenUnavailableException;
    AccessToken getAccessToken(Object tokenId) throws AccessTokenUnavailableException;

    void invalidate(Object tokenId);

    void stop();
}
