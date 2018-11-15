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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class FilesystemSecretRefresher implements AccessTokens {

    private static final Logger LOG = LoggerFactory.getLogger(FilesystemSecretRefresher.class);

    private static final String NOT_AVAILABLE_TEMPLATE = "No token available for tokenId '%s'";
    private static final String EXPIRED_TEMPLATE = "AccessToken for tokenId '%s' expired.";

    private static final String FIXED_TOKENS_ENV_VAR = "OAUTH2_ACCESS_TOKENS";

    protected final TokensConfiguration configuration;

    public FilesystemSecretRefresher(final TokensConfiguration configuration) {
        this.configuration = configuration;
    }

    public void start() {
        initializeFixedTokensFromEnvironment();
        LOG.info("Starting to refresh tokens regularly from filesystem ...");

        if (configuration.isValidateTokensOnStartup()) {
            final FilesystemReader<?> reader = new AccessTokenHandler(accessTokens,
                    new JwtFileSystemTokenContentExtractor())
                    .getFilesystemReader();

            reader.readFromFilesystem();
            final List<?> missing = findMissingTokens();
            if (!missing.isEmpty()) {
                throw new TokensMissingException(missing);
            }
        }
    }

    protected void initializeFixedTokensFromEnvironment() {
        final String csv = getFixedToken();
        if (csv != null && !csv.trim().isEmpty()) {
            LOG.info("Initializing fixed access tokens from {} environment variable..", FIXED_TOKENS_ENV_VAR);

            final String[] tokens = csv.split(",");
            for (final String token : tokens) {
                final String[] keyValue = token.split("=");
                if (keyValue.length == 2) {
                    LOG.info("Using fixed access token {}..", keyValue[0]);
                    accessTokens.put(keyValue[0], new AccessToken(keyValue[1], "fixed"));
                } else {
                    LOG.error("Could not create access token from {}", token);
                }
            }
        }
    }

    @Override
    public AccessToken getAccessToken(final Object tokenId) throws AccessTokenUnavailableException {
        final AccessToken token = accessTokens.get(tokenId);
        if (token == null) {
            throw new AccessTokenUnavailableException(format(NOT_AVAILABLE_TEMPLATE, tokenId.toString()));
        }

        if (token.isExpired()) {
            throw new AccessTokenUnavailableException(format(EXPIRED_TEMPLATE, tokenId.toString()));
        }

        return token;
    }

    private Optional<String> getFixedToken() {
        final String tokens = System.getProperty(FIXED_TOKENS_ENV_VAR);
        if (tokens == null) {
            return System.getenv(FIXED_TOKENS_ENV_VAR);
        }

        return tokens;
    }

    //@formatter:off
    protected List<?> findMissingTokens() {
        return configuration.getAccessTokenConfigurations()
                            .stream()
                            .map(AccessTokenConfiguration::getTokenId)
                            .filter(tokenId -> !accessTokens.containsKey(tokenId))
                            .collect(toList());
    }
    //@formatter:on

}
