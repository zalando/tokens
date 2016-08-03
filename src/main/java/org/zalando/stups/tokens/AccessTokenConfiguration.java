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

/**
 * Configuration for one single access token that is manages by the {@link AccessTokens} service.
 * It is used at configuration time and is retrieved via
 * {@link AccessTokensBuilder#manageToken(Object)}. It is possible to configure the desired
 * <i>scopes</i> (see https://tools.ietf.org/html/rfc6749#section-3.3 for details) as well as the
 * <i>grant type</i> (see https://tools.ietf.org/html/rfc6749#section-1.3 for details) for this
 * access token. It follows a fluent interface design pattern.
 *
 * Invoke {@link AccessTokenConfiguration#done()} after you finished configuring this access token
 * to return to the fluent interface of the {@link AccessTokensBuilder} to either add another
 * managed access token or add additional configuration.
 *
 */
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

	/**
	 * Add a single scope to this access token. Scopes will most of the time being expressed as
	 * plain strings. If the scope is not a {@link String} it {@link Object#toString()} method will
	 * be invoked when requesting the access token from the authorization server.
	 *
	 * @param scope  The scope to add for this access token. An {@link IllegalArgumentException}
	 *               will be thrown in case of <i>scope</i> being <i>null</i>.
	 * @return The same {@link AccessTokenConfiguration} instance this method has been called upon
	 * with the supplied <i>scope</i> added to the set of existing scopes.
     */
	public AccessTokenConfiguration addScope(final Object scope) {
		checkLock();
		scopes.add(notNull("scope", scope));
		return this;
	}

	/**
	 * Add multiple scopes to this access token. Refer to
	 * {@link AccessTokenConfiguration#addScope(Object)} for more detailed description of a scope.
	 *
	 * @param scopes  A {@link Collection} of scopes that should be added to this access token. An
	 *                {@link IllegalArgumentException} will be throws if either <i>scopes</i> being
	 *                <i>null</i> or {@link Collection#contains(Object)} is <i>true</i> for the
	 *                <i>null</i> arguments.
	 *                Please note that scopes are stored as {@link Set} internally, i.e. duplicates
	 *                with respect to {@link Object#hashCode()} and {@link Object#equals(Object)}
	 *                will be removed.
	 * @return The same {@link AccessTokenConfiguration} instance this method has been called upon
	 * with the supplied <i>scopes</i> added to the set of existing scopes.
     */
	public AccessTokenConfiguration addScopes(final Collection<?> scopes) {
		checkLock();
		this.scopes.addAll(noNullEntries("scopes", notNull("scopes", scopes)));
		return this;
	}

	/**
	 * Set the grant type to be used for this access token. For possible valid grant types see
	 * https://tools.ietf.org/html/rfc6749#section-1.3. Defaults to <i>password</i>.
	 *
	 * @param grantType  The <i>grant type</i> to be used when requesting an access token.
	 * @return The same {@link AccessTokenConfiguration} instance this method has been called upon
	 * with the supplied <i>grant type</i> set.
     */
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

	/**
	 * Finish configuring this access token, lock the configuration and return to the
	 * {@link AccessTokensBuilder} for further configuration of additional access tokens or other
	 * aspects.
	 *
	 * @return The {@link AccessTokensBuilder} that was used to create this
	 * {@link AccessTokenConfiguration} with an access token configured according to this instance.
	 */
	public AccessTokensBuilder done() {
		locked = true;
		return accessTokensBuilder;
	}

	/**
	 * Get the <i>grant type</i> for this access token.
	 *
	 * @return The grant type for this access token
	 */
	public String getGrantType() {
		return grantType;
	}
}
