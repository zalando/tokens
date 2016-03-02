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

import static java.util.concurrent.TimeUnit.MINUTES;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.stups.tokens.mcb.MCB;

/**
 * 
 * @author jbellmann
 *
 */
class TokenVerifyRunner implements Runnable, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(TokenVerifyRunner.class);

    private final TokenRefresherConfiguration configuration;
    private final Map<Object, AccessToken> accessTokens;
    private Set<Object> invalidTokenIds;

    private final MCB mcb;

    private TokenVerifier tokenVerifier;

    public TokenVerifyRunner(TokenRefresherConfiguration configuration, Map<Object, AccessToken> accessTokens,
            Set<Object> invalidTokenIds) {
        this.configuration = configuration;
        this.accessTokens = accessTokens;
        this.invalidTokenIds = invalidTokenIds;
        this.mcb = new MCB(this.configuration.getTokenVerifierMcbConfig());
        if (configuration.getTokenInfoUri() != null) {
            this.tokenVerifier = configuration.getTokenVerifierProvider().create(configuration.getTokenInfoUri(),
                    configuration.getHttpConfig(), configuration.getMetricsListener());
        } else {
            LOG.warn("No AccessToken-Verification enabled because no 'tokenInfoUri' was configured");
        }
    }

    @Override
    public void run() {
        if (tokenVerifier != null) {
            if (mcb.isClosed()) {
                for (final AccessTokenConfiguration tokenConfig : configuration.getAccessTokenConfigurations()) {
                    try {
                        final AccessToken accessToken = accessTokens.get(tokenConfig.getTokenId());

                        if (accessToken != null && olderThanMinute(accessToken)) {
                            String token = accessToken.getToken();
                            if (!tokenVerifier.isTokenValid(token)) {
                                invalidTokenIds.add(accessToken);
                                LOG.warn("Invalid Token scheduled for refresh : " + tokenConfig.getTokenId());
                            } else {
                                LOG.debug("Token for " + tokenConfig.getTokenId() + " still valid.");
                            }
                            mcb.onSuccess();
                        }
                    } catch (final Throwable t) {
                        LOG.warn("Unexpected problem during token verify run! TokenId : {}", tokenConfig.getTokenId(),
                                t);
                        mcb.onError();
                    }
                }
            } else {
                LOG.debug("MCB is open, skip check.");
            }
        } else {
            LOG.debug("No TokenVerifier configured, skipp check.");
        }
    }

    protected boolean olderThanMinute(AccessToken accessToken) {
        long diff = System.currentTimeMillis() - accessToken.getCreationTimestamp();
        return diff > MINUTES.toMillis(1) ? true : false;
    }

    @Override
    public void close() throws IOException {
        if (tokenVerifier != null) {
            tokenVerifier.close();
        }
    }

}
