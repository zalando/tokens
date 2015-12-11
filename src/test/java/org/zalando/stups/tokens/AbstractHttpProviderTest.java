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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AbstractHttpProviderTest {


    private TestHttpProvider httpProvider;

    private static class TestHttpProvider extends AbstractHttpProvider {
        @Override
        public AccessToken createToken(AccessTokenConfiguration tokenConfig) throws UnsupportedEncodingException {
            return null;
        }

        @Override
        public void close() throws IOException {

        }
    }

    @Before
    public void setUp() throws Exception {

        this.httpProvider = new TestHttpProvider();
    }

    @Test
    public void testJoinEmptyScopes() throws Exception {
        String scopes = httpProvider.joinScopes(new ArrayList<Object>());
        assertEquals("", scopes);
    }

    @Test
    public void testJoinScopes() throws Exception {
        List<Object> scopesList = new ArrayList<>();
        scopesList.add("test");
        scopesList.add("blubb");
        String scopes = httpProvider.joinScopes(scopesList);
        assertEquals("test blubb", scopes);
    }
}