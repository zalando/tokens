/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.tokens;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

public class AccessTokenConfigurationBuilder {
    private final Object tokenId;
    private final AccessTokensBuilder accessTokensBuilder;

    AccessTokenConfigurationBuilder(final Object tokenId, final AccessTokensBuilder accessTokensBuilder) {
        this.tokenId = tokenId;
        this.accessTokensBuilder = accessTokensBuilder;
    }

    public ScopeConfigurationBuilder.Static addScope(final String scope) {
        return new ScopeConfigurationBuilder.Static(this, new HashSet<>(singletonList(scope)));
    }

    public ScopeConfigurationBuilder.Static addScopes(final Collection<String> scopes) {
        return new ScopeConfigurationBuilder.Static(this, new HashSet<>(scopes));
    }

    public ScopeConfigurationBuilder.Dynamic addScope(final Supplier<String> scopeSupplier) {
        return new ScopeConfigurationBuilder.Dynamic(this, () -> new HashSet<>(singletonList(scopeSupplier.get())));
    }

    public ScopeConfigurationBuilder.Dynamic addScopes(final Supplier<Set<String>> scopeSupplier) {
        return new ScopeConfigurationBuilder.Dynamic(this, scopeSupplier);
    }

    AccessTokensBuilder build(AccessTokenConfiguration.ScopeConfiguration scopeConfiguration) {
        accessTokensBuilder.addAccessTokenConfigurations(new AccessTokenConfiguration(tokenId, scopeConfiguration));
        return accessTokensBuilder;
    }
}
