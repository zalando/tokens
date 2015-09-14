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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

abstract class ScopeConfigurationBuilder {

    protected final AccessTokenConfigurationBuilder builder;

    public ScopeConfigurationBuilder(final AccessTokenConfigurationBuilder builder) {
        this.builder = builder;
    }

    public AccessTokensBuilder done() {
        return builder.build(build());
    }

    protected abstract AccessTokenConfiguration.ScopeConfiguration build();

    static class Static extends ScopeConfigurationBuilder {
        private final Set<Object> scopes;

        public Static(final AccessTokenConfigurationBuilder builder,
                final Set<Object> scopes) {
            super(builder);
            this.scopes = scopes;
        }

        public Static addScope(final Object scope) {
            scopes.add(scope);
            return this;
        }

        public Static addScopes(final Collection<Object> newScopes) {
            scopes.addAll(newScopes);
            return this;
        }

        @Override
        protected AccessTokenConfiguration.ScopeConfiguration build() {
            return () -> Collections.unmodifiableSet(scopes);
        }
    }

    static class Dynamic extends ScopeConfigurationBuilder {

        private final Supplier<Set<Object>> scopeSupplier;

        public Dynamic(final AccessTokenConfigurationBuilder builder,
                final Supplier<Set<Object>> scopeSupplier) {
            super(builder);
            this.scopeSupplier = scopeSupplier;
        }

        @Override
        protected AccessTokenConfiguration.ScopeConfiguration build() {
            return () -> Collections.unmodifiableSet(scopeSupplier.get());
        }
    }
}
