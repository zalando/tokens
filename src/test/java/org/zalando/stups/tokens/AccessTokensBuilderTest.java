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

import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AccessTokensBuilderTest {

    @Test
    public void testMultipleTokens() {
        final AccessTokensBuilder builder = Tokens.createAccessTokensWithUri(URI.create("http://example.com"))
                .manageToken("token1")
                .addScope("scope")
                .done()
                .manageToken("token2")
                .addScope("scope")
                .done();

        final ArrayList<AccessTokenConfiguration> configurations = new ArrayList<>(builder.getAccessTokenConfigurations());
        assertThat(configurations, hasSize(2));

        final AccessTokenConfiguration configuration1 = configurations.get(0);
        final AccessTokenConfiguration configuration2 = configurations.get(1);

        assertThat(configuration1.getTokenId(), is((Object) "token1"));
        assertThat(configuration1.getScopes(), hasSize(1));
        assertThat(configuration2.getTokenId(), is((Object) "token2"));
        assertThat(configuration2.getScopes(), hasSize(1));
    }

    @Test
    public void testDynamicScope() {
        final AccessTokensBuilder builder = Tokens.createAccessTokensWithUri(URI.create("http://example.com"))
                .manageToken("token")
                .addScope(new Supplier<Object>() {
                    @Override
                    public Object get() {
                        return "scope";
                    }
                })
                .done();

        final AccessTokenConfiguration configuration = builder.getAccessTokenConfigurations().iterator().next();
        assertThat(configuration.getTokenId(), is((Object) "token"));
        assertThat(configuration.getScopes(), hasSize(1));
        assertThat(configuration.getScopes().iterator().next(), is((Object) "scope"));
    }

    @Test
    public void testDynamicScopes() {
        final AccessTokensBuilder builder = Tokens.createAccessTokensWithUri(URI.create("http://example.com"))
                .manageToken("token")
                .addScopes(new Supplier<Set<Object>>() {
                    @Override
                    public Set<Object> get() {
                        return new HashSet<Object>(asList("scope1", "scope2"));
                    }
                })
                .done();

        final AccessTokenConfiguration configuration = builder.getAccessTokenConfigurations().iterator().next();
        assertThat(configuration.getTokenId(), is((Object) "token"));
        assertThat(configuration.getScopes(), hasSize(2));

        final Iterator<Object> scopes = configuration.getScopes().iterator();
        assertThat(scopes.next(), is((Object) "scope1"));
        assertThat(scopes.next(), is((Object) "scope2"));
    }
}
