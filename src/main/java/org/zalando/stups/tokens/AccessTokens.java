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

/**
 * Use this interface to get retrieve and invalidate access tokens after having them built using
 * {@link AccessTokensBuilder}
 *
 * In most cases getting the access token string via @{@link AccessTokens#get(Object)} will be
 * sufficient. The returned string should be used as Bearer token with OAuth2.0 e.g.
 *
 * If you need the full access token including issue date as well as expire time you should invoke
 * {@link AccessTokens#getAccessToken(Object)}.
 *
 * If you want to invalidate a specific access token to whatever reasons you can use
 * {@link AccessTokens#invalidate(Object)}.
 *
 * The <i>tokenId</i> object used for all three methods must be an object that is equal to the
 * object used when defining the token via {@link AccessTokensBuilder#manageToken(Object)}. If you
 * are referring to tokens by {@link String} simply use a {@link String} with equals characters here.
 *
 * @author sarnowski
 * @author jbellmann
 * @author duergner
 *
 */
public interface AccessTokens {

    /**
     * Get a valid and up to date access token for the supplied <i>tokenId</i>. The returned {@link String}
     * can be used as an OAuth2.0 Bearer token e.g.
     *
     * @param tokenId  The <i>tokenId</i> to get the access token for. The supplied <i>tokenId</i>
     *                 must be equal to the <i>tokenId</i> as supplied on
     *                 {@link AccessTokensBuilder#manageToken(Object)} with respect to
     *                 {@link Object#equals(Object)}.
     * @return A {@link String} representation of the access token for the supplied <i>tokenId</i>.
     * The return value will always be a non null {@link String}.
     * @throws AccessTokenUnavailableException Thrown inn case there is no access token available
     * for the supplied <i>tokenId</i> either because there was no such <i>tokenId</i> configured
     * or getting one from the authorization server was not possible.
     */
    String get(Object tokenId) throws AccessTokenUnavailableException;

    /**
     * Get a full {@link AccessToken} not just the {@link String} representation for the supplied
     * <i>tokenId</i>.
     *
     * @param tokenId  The <i>tokenId</i> to get the access token for. The supplied <i>tokenId</i>
     *                 must be equal to the <i>tokenId</i> as supplied on
     *                 {@link AccessTokensBuilder#manageToken(Object)} with respect to
     *                 {@link Object#equals(Object)}.
     * @return An {@link AccessToken} for the supplied <i>tokenId</i>. The return value will always
     * be a non null value.
     * @throws AccessTokenUnavailableException Thrown inn case there is no access token available
     * for the supplied <i>tokenId</i> either because there was no such <i>tokenId</i> configured
     * or getting one from the authorization server was not possible.
     */
    AccessToken getAccessToken(Object tokenId) throws AccessTokenUnavailableException;

    /**
     * Invalidate the current {@link AccessToken} stored for the supplied <i>tokenId</i>. This will
     * cause {@link AccessTokens#get(Object)} and {@link AccessTokens#getAccessToken(Object)} to
     * throw an {@link AccessTokenUnavailableException} until a new access token for this
     * <i>tokenId</i> could be fetched from the authorization server.
     *
     * @param tokenId   The <i>tokenId</i> to invalidate. If no such <i>tokenId</i> has been
     *                  configured via the {@link AccessTokensBuilder#manageToken(Object)} method
     *                  this method will simply be a no-op method.
     */
    void invalidate(Object tokenId);

    /**
     * Instruct the underlying implementation to stop refreshing of access tokens.
     *
     * PLEASE NOTE: Implementations may choose to invalidate all access tokens when this method has
     * been invoked.
     */
    void stop();
}
