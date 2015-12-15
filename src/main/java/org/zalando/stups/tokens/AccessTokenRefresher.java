/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.tokens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class AccessTokenRefresher implements AccessTokens, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AccessTokenRefresher.class);

    private static final long ONE_YEAR_SECONDS =  TimeUnit.DAYS.toSeconds(365);
    private static final String FIXED_TOKENS_ENV_VAR = "OAUTH2_ACCESS_TOKENS";

    private final TokenRefresherConfiguration configuration;
    private final ScheduledExecutorService scheduler;
    private HttpProvider httpProvider;
    private final int schedulingPeriod;

    private ConcurrentHashMap<Object, AccessToken> accessTokens = new ConcurrentHashMap<Object, AccessToken>();

    public AccessTokenRefresher(final TokenRefresherConfiguration configuration) {
        this.configuration = configuration;
        this.schedulingPeriod = configuration.getSchedulingPeriod();
        scheduler = Executors.newSingleThreadScheduledExecutor();

    }

    protected void initializeFixedTokensFromEnvironment() {
        final String csv = getFixedToken();
        if (csv != null) {
            LOG.info("Initializing fixed access tokens from {} environment variable..", FIXED_TOKENS_ENV_VAR);

            final String[] tokens = csv.split(",");
            final long expiresInSeconds = ONE_YEAR_SECONDS;
            final Date validUntil = new Date(System.currentTimeMillis() + (expiresInSeconds * 1000));
            for (String token : tokens) {
                final String[] keyValue = token.split("=");
                if (keyValue.length == 2) {
                    LOG.info("Using fixed access token {}..", keyValue[0]);
                    accessTokens.put(keyValue[0], new AccessToken(keyValue[1], "fixed", expiresInSeconds, validUntil));
                } else {
                    LOG.error("Could not create access token from {}", token);
                }
            }
        }
    }

    // visible for testing
    protected String getFixedToken() {
        final String tokens = System.getProperty(FIXED_TOKENS_ENV_VAR);
        if (tokens == null) {
            return System.getenv(FIXED_TOKENS_ENV_VAR);
        }

        return tokens;
    }

    void start() {
        initializeFixedTokensFromEnvironment();
        LOG.info("Starting to refresh tokens regularly...");
        final ClientCredentials clientCredentials = configuration.getClientCredentialsProvider().get();
        UserCredentials userCredentials = null;
        if (configuration.getUserCredentialsProvider() != null) {
            userCredentials = configuration.getUserCredentialsProvider().get();
        }
        httpProvider = configuration.getHttpProviderFactory().create(clientCredentials,
                userCredentials, configuration.getAccessTokenUri(), configuration.getHttpConfig());
        run();

        // #10, increase 'period' to 5 to avoid flooding the endpoint
        scheduler.scheduleAtFixedRate(this, 1, schedulingPeriod, TimeUnit.SECONDS);
    }

    static int percentLeft(final AccessToken token) {
        final long now = System.currentTimeMillis();
        final long validUntil = token.getValidUntil().getTime();
        final long hundredPercentSeconds = token.getInitialValidSeconds();
        final long secondsLeft = (validUntil - now) / 1000;
        return (int) ((double) secondsLeft / (double) hundredPercentSeconds * (double) 100);
    }

    static boolean shouldRefresh(final AccessToken token, final TokenRefresherConfiguration configuration) {
        return percentLeft(token) <= configuration.getRefreshPercentLeft();
    }

    static boolean shouldWarn(final AccessToken token, final TokenRefresherConfiguration configuration) {
        return percentLeft(token) <= configuration.getWarnPercentLeft();
    }

    @Override
    public void run() {
        try {
            for (final AccessTokenConfiguration tokenConfig : configuration.getAccessTokenConfigurations()) {
                final AccessToken oldToken = accessTokens.get(tokenConfig.getTokenId());

                // TODO optionally check with tokeninfo endpoint regularly (every x% of time)
                if (oldToken == null || shouldRefresh(oldToken, configuration)) {
                    try {
                        LOG.trace("Refreshing access token {}...", tokenConfig.getTokenId());

                        final AccessToken newToken = createToken(tokenConfig);
                        accessTokens.put(tokenConfig.getTokenId(), newToken);
                        LOG.info("Refreshed access token {}.", tokenConfig.getTokenId());
                    } catch (final Throwable t) {
                        if (oldToken == null || shouldWarn(oldToken, configuration)) {
                            LOG.warn("Cannot refresh access token {} because {}.", tokenConfig.getTokenId(), t);
                        } else {
                            LOG.info("Cannot refresh access token {} because {}.", tokenConfig.getTokenId(), t);
                        }
                    }
                }

            }
        } catch (final Throwable t) {
            LOG.error("Unexpected problem during token refresh run!", t);
        }
    }


    private AccessToken createToken(final AccessTokenConfiguration tokenConfig) {
        try {
            return httpProvider.createToken(tokenConfig);
        } catch (RuntimeException | UnsupportedEncodingException e) {
            throw new AccessTokenEndpointException(e.getMessage(), e);
        }
    }

    @Override
    public String get(final Object tokenId) throws AccessTokenUnavailableException {
        return getAccessToken(tokenId).getToken();
    }

    @Override
    public AccessToken getAccessToken(final Object tokenId) throws AccessTokenUnavailableException {
        final AccessToken token = accessTokens.get(tokenId);
        if (token == null) {
            throw new AccessTokenUnavailableException("no token available");
        }

        if (token.isExpired()) {
            throw new AccessTokenUnavailableException("token expired");
        }

        return token;
    }

    @Override
    public void invalidate(final Object tokenId) {
        accessTokens.remove(tokenId);
    }

    @Override
    public void stop() {
        scheduler.shutdown();
        try {
            httpProvider.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
