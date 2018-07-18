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

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractAccessTokenRefresherTest {

    private static final String FIXED_TOKENS = "one=DSAFDASASDFDA,two=DSFADFADFADFADFA,three=ADAFDAFADFAFEWRDFADFASDF";
    private static final String EXPECTED_MESSAGE = "No token available for tokenId 'four'. Tokens are available for the following tokenIds [one, two, three]";

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testListToString() {
        List<String> keys = Arrays.asList("One", "Two", "Three");
        Assertions.assertThat(keys.toString()).isEqualTo("[One, Two, Three]");
    }

    @Test
    public void testExceptionMessage() {
        environmentVariables.set("OAUTH2_ACCESS_TOKENS", FIXED_TOKENS);
        TokenRefresherConfiguration configuration = Mockito.mock(TokenRefresherConfiguration.class);
        AbstractAccessTokenRefresher refresher = new TestTokenRefresher(configuration);
        refresher.initializeFixedTokensFromEnvironment();
        String availableTokenIds = refresher.getAvailableTokenIds();
        Assertions.assertThat(availableTokenIds).isEqualTo("[one, two, three]");
        try {
            refresher.get("four");
            Assertions.fail("Expect an exception to be thrown because the token 'four' is not available");
        } catch (AccessTokenUnavailableException e) {
            String message = e.getMessage();
            Assertions.assertThat(message).isEqualTo(EXPECTED_MESSAGE);
        }

    }

    static class TestTokenRefresher extends AbstractAccessTokenRefresher {

        private static final Logger LOG = LoggerFactory.getLogger(TestTokenRefresher.class);

        public TestTokenRefresher(TokenRefresherConfiguration configuration) {
            super(configuration);
        }

        @Override
        public void start() {
            LOG.info("START {} ....", getClass().getName());
        }

    }
}
