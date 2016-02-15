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

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

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

    private final MCB mcb;

    private TokenVerifier tokenVerifier;

    public TokenVerifyRunner(TokenRefresherConfiguration configuration, Map<Object, AccessToken> accessTokens) {
        this.configuration = configuration;
        this.accessTokens = accessTokens;
        this.mcb = new MCB();
        if (configuration.getTokenInfoUri() != null) {
            this.tokenVerifier = configuration.getTokenVerifierProvider().create(configuration.getTokenInfoUri(),
                    configuration.getHttpConfig());
        } else {
            LOG.warn("No AccessToken-Verification enabled because no tokenInfoUri was configured");
        }
    }

    @Override
    public void run() {
        if (tokenVerifier != null) {
            if (mcb.isClosed()) {
                for (final AccessTokenConfiguration tokenConfig : configuration.getAccessTokenConfigurations()) {
                    try {
                        final AccessToken accessToken = accessTokens.get(tokenConfig.getTokenId());

                        if (accessToken != null) {
                            String token = accessToken.getToken();
                            if (!tokenVerifier.isTokenValid(token)) {
                                accessTokens.remove(tokenConfig.getTokenId());
                            }
                            mcb.onSuccess();
                        }
                    } catch (final Throwable t) {
                        LOG.warn("Unexpected problem during token verify run! TokenId : {}", tokenConfig.getTokenId(),
                                t);
                        mcb.onError();
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (tokenVerifier != null) {
            ((Closeable) tokenVerifier).close();
        }
    }

}
