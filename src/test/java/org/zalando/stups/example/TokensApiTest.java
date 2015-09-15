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
package org.zalando.stups.example;

import org.junit.Test;
import org.zalando.stups.tokens.AccessTokens;
import org.zalando.stups.tokens.Supplier;
import org.zalando.stups.tokens.Tokens;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

/**
 * Testing whether the API actually provides the functionality we expose in the README
 */
public class TokensApiTest {

    @Test
    public void testExampleUsage() throws URISyntaxException {
        AccessTokens tokens = Tokens.createAccessTokensWithUri(new URI("https://example.com/access_tokens"))
                .manageToken("exampleRW")
                .addScope("read")
                .addScope("write")
                .done()
                .manageToken("exampleRO")
                .addScope("read")
                .done()
                .start();
    }

    @Test
    public void testDynamicUsage() throws URISyntaxException {
        AccessTokens tokens = Tokens.createAccessTokensWithUri(new URI("https://example.com/access_tokens"))
                .manageToken("a")
                .addScope(new Supplier<Object>() {
                    @Override
                    public Object get() {
                        return "x";
                    }
                })
                .done()
                .manageToken("b")
                .addScopes(new Supplier<Set<Object>>() {
                    @Override
                    public Set<Object> get() {
                        return new HashSet<>();
                    }
                })
                .done()
                .start();
    }

}
