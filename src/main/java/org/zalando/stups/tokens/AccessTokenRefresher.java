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

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.stups.tokens.mcb.MCB;
import org.zalando.stups.tokens.util.Metrics;
import org.zalando.stups.tokens.util.Objects;

class AccessTokenRefresher extends AbstractAccessTokenRefresher implements AccessTokens, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AccessTokenRefresher.class);

    private static final String METRICS_KEY_PREFIX = "tokens.refresher";

    private final MCB mcb;

    private final Set<Object> invalidTokens = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());

    private final TokenVerifyRunner verifyRunner;

    private final MetricsListener metricsListener;

    public AccessTokenRefresher(final TokenRefresherConfiguration configuration) {
        super(configuration);
        this.metricsListener = configuration.getMetricsListener();
        this.verifyRunner = new TokenVerifyRunner(configuration, accessTokens, invalidTokens);
        this.mcb = new MCB(configuration.getTokenRefresherMcbConfig());
    }

    @Override
    public void start() {
        initializeFixedTokensFromEnvironment();
        LOG.info("Starting to refresh tokens regularly...");
        run();

        // #10, increase 'period' to 5 to avoid flooding the endpoint
        scheduler.scheduleAtFixedRate(this, 1, configuration.getSchedulingPeriod(),
                configuration.getSchedulingTimeUnit());

        // #36
        scheduler.scheduleAtFixedRate(verifyRunner, 5, configuration.getTokenVerifierSchedulingPeriod(),
                configuration.getTokenVerifierSchedulingTimeUnit());
    }

    static int percentLeft(final AccessToken token) {
        final long now = System.currentTimeMillis();
        final long validUntil = token.getValidUntil().getTime();
        final long hundredPercentSeconds = token.getInitialValidSeconds();
        final long secondsLeft = (validUntil - now) / 1000;
        return (int) ((double) secondsLeft / (double) hundredPercentSeconds * 100);
    }

    static boolean shouldRefresh(final AccessToken token, final TokenRefresherConfiguration configuration) {
        return percentLeft(token) <= configuration.getRefreshPercentLeft();
    }

    static boolean shouldWarn(final AccessToken token, final TokenRefresherConfiguration configuration) {
        return percentLeft(token) <= configuration.getWarnPercentLeft();
    }

    protected boolean isInvalid(final AccessToken token) {
        if (token == null) {
            return false;
        }
        return invalidTokens.contains(token);
    }

    @Override
    public void run() {
        if (mcb.isClosed()) {
            for (final AccessTokenConfiguration tokenConfig : configuration.getAccessTokenConfigurations()) {
                try {
                    final AccessToken oldToken = accessTokens.get(tokenConfig.getTokenId());

                    if (oldToken == null || shouldRefresh(oldToken, configuration) || isInvalid(oldToken)) {
                        try {
                            LOG.trace("Refreshing access token {}...", tokenConfig.getTokenId());

                            final AccessToken newToken = createToken(tokenConfig);
                            // validate
                            Objects.notNull("newToken", newToken);
                            accessTokens.put(tokenConfig.getTokenId(), newToken);
                            if (oldToken != null) {
                                invalidTokens.remove(oldToken);
                            }
                            mcb.onSuccess();
                            LOG.info("Refreshed access token {}.", tokenConfig.getTokenId());
                        } catch (final Throwable t) {
                            if (oldToken == null || shouldWarn(oldToken, configuration)) {
                                LOG.warn("Cannot refresh access token " + tokenConfig.getTokenId(), t);
                            } else {
                                LOG.info("Cannot refresh access token {}", tokenConfig.getTokenId(), t);
                            }
                            mcb.onError();
                        }
                    }
                } catch (Throwable t) {
                    mcb.onError();
                    LOG.warn("Unexpected problem during token refresh run! TokenId: " + tokenConfig.getTokenId(), t);
                }
            }
        } else {
            LOG.debug("{} is open, skip refresh", mcb.getName());
        }
    }

    private AccessToken createToken(final AccessTokenConfiguration tokenConfig) {
        final ClientCredentials clientCredentials = configuration.getClientCredentialsProvider().get();
        UserCredentials userCredentials = null;
        if (configuration.getUserCredentialsProvider() != null) {
            userCredentials = configuration.getUserCredentialsProvider().get();
        }

        long start = System.currentTimeMillis();
        boolean success = true;
        try (final HttpProvider httpProvider = buildHttpProvider(clientCredentials, userCredentials)) {
            return httpProvider.createToken(tokenConfig);
        } catch (RuntimeException | IOException e) {
            success = false;
            throw new AccessTokenEndpointException(e.getMessage(), e);
        } finally {
            long diff = System.currentTimeMillis() - start;
            metricsListener.submitToTimer(Metrics.buildMetricsKey(METRICS_KEY_PREFIX, success), diff);
        }
    }

    private HttpProvider buildHttpProvider(ClientCredentials clientCredentials, UserCredentials userCredentials){
        return configuration.getHttpProviderFactory().create(clientCredentials,
                userCredentials, configuration.getAccessTokenUri(), configuration.getHttpConfig());
    }
}
