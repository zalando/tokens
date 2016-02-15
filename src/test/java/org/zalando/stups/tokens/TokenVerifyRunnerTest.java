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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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

            accessTokens.put("TOKEN_" + i, new AccessToken("12345678", "Bearer", 1, new Date()));
        }
        return configurations;
    }

    @Test
    public void create() {
        TokenVerifier verifier = Mockito.mock(TokenVerifier.class);
        Mockito.when(configuration.getTokenInfoUri()).thenReturn(tokenInfoUri);
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
        Assertions.assertThat(accessTokens.keySet().size()).isEqualTo(2);
    }
}
