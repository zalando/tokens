package org.zalando.stups.tokens;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class AccessTokenRefresher implements AccessTokens, Runnable {
    private final AccessTokensBuilder configuration;
    private final ScheduledExecutorService scheduler;

    private ConcurrentHashMap<Object, AccessToken> accessTokens = new ConcurrentHashMap<Object, AccessToken>();

    public AccessTokenRefresher(final AccessTokensBuilder configuration) {
        this.configuration = configuration;

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(this, 1, TimeUnit.SECONDS);
    }

    private int percentLeft(final AccessToken token) {
        final long now = System.currentTimeMillis();
        final long validUntil = token.getValidUntil().getTime();
        final long hundredPercentSeconds = token.getInitialValidSeconds();
        final long secondsLeft = validUntil - now;
        return (int) (secondsLeft / hundredPercentSeconds * 100);
    }

    private boolean shouldRefresh(final AccessToken token) {
        return percentLeft(token) <= configuration.getRefreshPercentLeft();
    }

    private boolean shouldWarn(final AccessToken token) {
        return percentLeft(token) <= configuration.getWarnPercentLeft();
    }

    @Override
    public void run() {
        try {
            for (final AccessTokenConfiguration tokenConfig : configuration.getAccessTokenConfigurations()) {
                final AccessToken oldToken = accessTokens.get(tokenConfig.getTokenId());
                // TODO optionally check with tokeninfo endpoint regularly (every x% of time)
                if (oldToken == null || shouldRefresh(oldToken)) {
                    try {
                        final AccessToken newToken = createToken(tokenConfig);
                        accessTokens.put(tokenConfig.getTokenId(), newToken);
                        // TODO log debug, token refreshed
                    } catch (final Throwable t) {
                        if (oldToken == null || shouldWarn(oldToken)) {
                            // TODO log warning about problems with t
                        }
                    }
                }
            }
        } catch (final Throwable t) {
            // TODO log error, something really bad happened
        }
    }

    private AccessToken createToken(final AccessTokenConfiguration tokenConfig) {
        // TODO go to accessToken URI with credentials and get new access token
        throw new UnsupportedOperationException();
    }

    @Override
    public String get(Object tokenId) throws AccessTokenUnavailable {
        return getAccessToken(tokenId).getToken();
    }

    @Override
    public AccessToken getAccessToken(Object tokenId) throws AccessTokenUnavailable {
        final AccessToken token = accessTokens.get(tokenId);
        if (token == null || token.isExpired()) {
            throw new AccessTokenUnavailable();
        }
        return token;
    }

    @Override
    public void stop() {
        scheduler.shutdown();
    }
}
