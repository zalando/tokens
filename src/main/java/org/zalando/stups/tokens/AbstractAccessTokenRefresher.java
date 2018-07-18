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

import static java.lang.String.format;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAccessTokenRefresher implements AccessTokens {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAccessTokenRefresher.class);

    private static final String NOT_AVAILABLE_TEMPLATE = "No token available for tokenId '%s'. Tokens are available for the following tokenIds %s";
    private static final String EXPIRED_TEMPLATE = "AccessToken for tokenId '%s' expired.";

    private static final long ONE_YEAR_SECONDS = TimeUnit.DAYS.toSeconds(365);
    private static final String FIXED_TOKENS_ENV_VAR = "OAUTH2_ACCESS_TOKENS";

    protected final TokenRefresherConfiguration configuration;
    protected final ScheduledExecutorService scheduler;

    protected final ConcurrentHashMap<Object, AccessToken> accessTokens = new ConcurrentHashMap<>();

    private String availableTokenIds;

    public AbstractAccessTokenRefresher(TokenRefresherConfiguration configuration) {
        this.configuration = configuration;
        this.scheduler = configuration.getExecutorService();
    }

    protected void initializeFixedTokensFromEnvironment() {
        final String csv = getFixedToken();
        if (csv != null && !csv.trim().isEmpty()) {
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

    public abstract void start();

    protected String getFixedToken() {
        final String tokens = System.getProperty(FIXED_TOKENS_ENV_VAR);
        if (tokens == null) {
            return System.getenv(FIXED_TOKENS_ENV_VAR);
        }

        return tokens;
    }

    @Override
    public String get(final Object tokenId) throws AccessTokenUnavailableException {
        return getAccessToken(tokenId).getToken();
    }

    @Override
    public AccessToken getAccessToken(final Object tokenId) throws AccessTokenUnavailableException {
        final AccessToken token = accessTokens.get(tokenId);
        if (token == null) {
            throw new AccessTokenUnavailableException(format(NOT_AVAILABLE_TEMPLATE, tokenId.toString(), getAvailableTokenIds()));
        }

        if (token.isExpired()) {
            throw new AccessTokenUnavailableException(format(EXPIRED_TEMPLATE, tokenId.toString()));
        }

        return token;
    }

    protected String getAvailableTokenIds() {
        if(availableTokenIds == null) {
            List<String> tokenIds = Lists.newArrayList();
            accessTokens.forEachKey(Long.MAX_VALUE, key -> {
                tokenIds.add(key.toString());
            });
            availableTokenIds = tokenIds.isEmpty() ? "[]" : tokenIds.toString();
        }
        return availableTokenIds;
    }

    @Override
    public void invalidate(final Object tokenId) {
        accessTokens.remove(tokenId);
    }

    @Override
    public void stop() {
        scheduler.shutdown();
    }
}
