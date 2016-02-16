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
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.stups.tokens.mcb.MCBConfig;

public class TokenVerifyRunnerTest {

    TokenRefresherConfiguration configuration;

    private final URI tokenInfoUri = URI.create("http://localhost/access_token");
    private TokenVerifierProvider tokenVerifierProvider;
    private Map<Object, AccessToken> accessTokens = new ConcurrentHashMap<>();
    private Set<Object> invalidTokens = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());

    @Before
    public void setUp() {
        configuration = Mockito.mock(TokenRefresherConfiguration.class);
        tokenVerifierProvider = Mockito.mock(TokenVerifierProvider.class);
        Mockito.when(configuration.getHttpConfig()).thenReturn(new HttpConfig());
        Mockito.when(configuration.getTokenVerifierProvider()).thenReturn(tokenVerifierProvider);
        Mockito.when(configuration.getAccessTokenConfigurations()).thenReturn(getAccessTokenConfigurations());
    }

    private Set<AccessTokenConfiguration> getAccessTokenConfigurations() {
        AccessTokensBuilder accessTokensBuilder = Mockito.mock(AccessTokensBuilder.class);
        Set<AccessTokenConfiguration> configurations = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            AccessTokenConfiguration configuration = new AccessTokenConfiguration("TOKEN_" + i, accessTokensBuilder);
            configuration = configuration.addScope("read_all");
            configurations.add(configuration);
            long creatIonTimestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);
            accessTokens.put("TOKEN_" + i, new AccessToken("12345678", "Bearer", 1, new Date(), creatIonTimestamp));
        }
        return configurations;
    }

    @Test
    public void create() {
        TokenVerifier verifier = Mockito.mock(TokenVerifier.class);
        Mockito.when(configuration.getTokenInfoUri()).thenReturn(tokenInfoUri);
        Mockito.when(configuration.getTokenVerifierMcbConfig()).thenReturn(new MCBConfig.Builder().build());
        Mockito.when(tokenVerifierProvider.create(Mockito.any(URI.class), Mockito.any(HttpConfig.class)))
                .thenReturn(verifier);
        Mockito.when(verifier.isTokenValid(Mockito.anyString())).thenReturn(true).thenReturn(false).thenReturn(true);
        TokenVerifyRunner runner = new TokenVerifyRunner(configuration, accessTokens, invalidTokens);
        // execute
        runner.run();
        runner.run();
        runner.run();

        try {
            runner.close();
        } catch (IOException e) {
            // close
        }
        Mockito.verify(verifier, Mockito.atLeast(3)).isTokenValid(Mockito.anyString());
        Assertions.assertThat(invalidTokens.size()).isEqualTo(1);
    }

    @Test
    public void olderThanMinute() {
        TokenVerifyRunner runner = new TokenVerifyRunner(configuration, accessTokens, invalidTokens);
        AccessToken accessToken = new AccessToken("token", "Bearer", 13, new Date());
        boolean result = runner.olderThanMinute(accessToken);
        Assertions.assertThat(result).isFalse();

        long creatIonTimestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);
        AccessToken accessToken2 = new AccessToken("token2", "Bearer", 13, new Date(), creatIonTimestamp);
        boolean result2 = runner.olderThanMinute(accessToken2);
        Assertions.assertThat(result2).isTrue();

    }
}
