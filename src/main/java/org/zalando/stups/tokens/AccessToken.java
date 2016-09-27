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

import java.util.Date;

/**
 * "Model" class for an access token that will include some additional information about the token
 * beside just being a {@link String} value that can be used as a <i>Bearer</i> token with OAuth2.0
 */
public class AccessToken {
    private final String token;
    private final String type;
    private final long initialValidSeconds;
    private final Date validUntil;
    private final long creationTimestamp;

    /**
     * Create a new instance with the supplied data
     *
     * @param token                The {@link String} token value for this {@link AccessToken}
     * @param type                 The type of the {@link AccessToken}, e.g. <i>Bearer</i>
     * @param initialValidSeconds  The number of seconds this {@link AccessToken} is still valid
     * @param validUntil           The point in time until this {@link AccessToken} will be valid
     */
    public AccessToken(final String token, final String type, final long initialValidSeconds, final Date validUntil) {
        this(token, type, initialValidSeconds, validUntil, System.currentTimeMillis());
    }

    protected AccessToken(final String token, final String type, final long initialValidSeconds, final Date validUntil,
            long creationTimestamp) {
        this.token = token;
        this.type = type;
        this.initialValidSeconds = initialValidSeconds;
        this.validUntil = validUntil;
        this.creationTimestamp = creationTimestamp;
    }

    /**
     * Get the token value of the {@link AccessToken}
     *
     * @return    The <i>token</i> {@link String} value of this {@link AccessToken}; will always be
     * a non null value
     */
    public String getToken() {
        return token;
    }

    /**
     * Get the type of the {@link AccessToken}
     *
     * @return  The <i>type</i> of this {@link AccessToken}, e.g. <i>Bearer</i>; will always be a
     * non null value
     */
    public String getType() {
        return type;
    }

    /**
     * The number of seconds this {@link AccessToken} was initially valid
     *
     * @return  The number of seconds this {@link AccessToken} was initially valid; will always be
     * a positive <i>long</i> value
     */
    public long getInitialValidSeconds() {
        return initialValidSeconds;
    }

    /**
     * The point in time until when this {@link AccessToken} is valid
     *
     * @return  A {@link Date} that will describe a point in time in the future in case this
     * {@link AccessToken} is not yet expired (see {@link AccessToken#isExpired()}).
     */
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     * Tell whether this {@link AccessToken} is expired.
     *
     * @return  Will return {@link Boolean#FALSE} if this {@link AccessToken} is still valid, i.e.
     * the {@link Date} as returned by {@link AccessToken#getValidUntil()} describes a point of time
     * in the future; {@link Boolean#TRUE} otherwise
     */
    public boolean isExpired() {
        return validUntil != null && validUntil.before(new Date());
    }

    /**
     * Get the UNIX timestamp (in milliseconds) when this {@link AccessToken} was created.
     *
     * @return  The number of seconds since 1970-01-01 00:00:00.000 when this {@link AccessToken}
     * was created.
     */
    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    @Override
    public String toString() {
        return "AccessToken{" +
                "token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", validUntil=" + validUntil +
                '}';
    }
}
