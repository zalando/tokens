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
import java.io.UnsupportedEncodingException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * 
 * @author jbellmann
 *
 */
public class UserAgentTest {

    @Test
    public void testUserAgent() {
        assertValue(new UserAgent().get());
    }

    @Test
    public void testUserAgentFromHttpProvider() {
        assertValue(MySimpleHttpProvider.USER_AGENT.get());
    }

    private void assertValue(String value) {
        Assertions.assertThat(value).isNotNull();
        Assertions.assertThat(value).startsWith(UserAgent.PREFIX);
        System.out.println(value);
    }

    class MySimpleHttpProvider implements HttpProvider {

        @Override
        public void close() throws IOException {

        }

        @Override
        public AccessToken createToken(AccessTokenConfiguration tokenConfig) throws UnsupportedEncodingException {
            return null;
        }

    }

}
