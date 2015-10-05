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
import java.util.HashSet;
import java.util.Set;

public class AccessTokenConfiguration {
    private final Object tokenId;
    private final AccessTokensBuilder accessTokensBuilder;

    private final Set<Object> scopes = new HashSet<>();

    private boolean locked = false;

    AccessTokenConfiguration(final Object tokenId, final AccessTokensBuilder accessTokensBuilder) {
        this.tokenId = tokenId;
        this.accessTokensBuilder = accessTokensBuilder;
    }

    private void checkLock() {
        if (locked) {
            throw new IllegalStateException("scope configuration already done");
        }
    }

    private void checkNotNull(final String name, final Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    public AccessTokenConfiguration addScope(final Object scope) {
        checkLock();
        checkNotNull("scope", scope);
        scopes.add(scope);
        return this;
    }

    public AccessTokenConfiguration addScopes(final Collection<?> scopes) {
        checkLock();
        checkNotNull("scopes", scopes);
        this.scopes.addAll(scopes);
        return this;
    }

    Object getTokenId() {
        return tokenId;
    }

    Set<Object> getScopes() {
        return Collections.unmodifiableSet(scopes);
    }

    public AccessTokensBuilder done() {
        locked = true;
        return accessTokensBuilder;
    }
}
