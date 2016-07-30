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

import static org.zalando.stups.tokens.util.Objects.notNull;

import java.net.URI;

/**
 * Use e.g. {@link Tokens#createAccessTokensWithUri(URI)} to create an {@link AccessTokensBuilder}
 * which can be used to define multiple access tokens via {@link AccessTokensBuilder#manageToken(Object)}
 * that are managed and refreshed.
 *
 * @author jbellmann
 * @author sarnowski
 * @author duergner
 */
public final class Tokens {
    private Tokens() {
    }

    /**
     * Create a new {@link AccessTokensBuilder} which can be used to define multiple <i>tokenId</i>s
     * which can be used to retrieved access tokens from the supplied <i>accessTokenUri</i> which
     * may have e.g. different <i>scopes</i>.
     *
     * @param accessTokenUri  The <i>accessTokenUri</i> to be used by the {@link AccessTokens}
     *                        implementation created by the returned {@link AccessTokensBuilder} to
     *                        get and refresh access tokens for the various defined <i>tokenId</i>s
     * @return An {@link AccessTokensBuilder} which may be used to configure multiple <i>tokenIds</i>s
     * with various <i>scopes</i> that are managed and refreshed automatically.
     */
    public static AccessTokensBuilder createAccessTokensWithUri(final URI accessTokenUri) {
        return new AccessTokensBuilder(notNull("accessTokenUri", accessTokenUri));
    }
}
