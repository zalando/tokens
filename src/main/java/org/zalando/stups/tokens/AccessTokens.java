package org.zalando.stups.tokens;

public interface AccessTokens {
    String get(Object tokenId) throws AccessTokenUnavailable;
    AccessToken getAccessToken(Object tokenId) throws AccessTokenUnavailable;

    void stop();
}
