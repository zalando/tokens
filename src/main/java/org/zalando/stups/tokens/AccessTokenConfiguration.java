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

import static org.zalando.stups.tokens.util.Objects.noNullEntries;
import static org.zalando.stups.tokens.util.Objects.notBlank;
import static org.zalando.stups.tokens.util.Objects.notNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.zalando.stups.tokens.util.Objects;

public class AccessTokenConfiguration {
	protected static final String DEFAULT_GRANT_TYPE = "password";
	private final Object tokenId;
	private final AccessTokensBuilder accessTokensBuilder;
	private String grantType;

	private final Set<Object> scopes = new HashSet<>();

	private boolean locked = false;

	AccessTokenConfiguration(final Object tokenId, final AccessTokensBuilder accessTokensBuilder) {
		this(tokenId, accessTokensBuilder, DEFAULT_GRANT_TYPE);
	}

	AccessTokenConfiguration(final Object tokenId, final AccessTokensBuilder accessTokensBuilder,
			final String grantType) {
		this.tokenId = notNull("tokenId", tokenId);
		this.accessTokensBuilder = notNull("accessTokenBuilder", accessTokensBuilder);
		this.grantType = notBlank("grantType", notNull("grantType", grantType));
	}

	private void checkLock() {
		if (locked) {
			throw new IllegalStateException("scope configuration already done");
		}
	}

	public AccessTokenConfiguration addScope(final Object scope) {
		checkLock();
		scopes.add(notNull("scope", scope));
		return this;
	}

	public AccessTokenConfiguration addScopes(final Collection<?> scopes) {
		checkLock();
		this.scopes.addAll(noNullEntries("scopes", notNull("scopes", scopes)));
		return this;
	}

	public AccessTokenConfiguration withGrantType(final String grantType) {
		checkLock();
		this.grantType = Objects.notBlank("grantType", notNull("grantType", grantType));
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

	public String getGrantType() {
		return grantType;
	}
}
