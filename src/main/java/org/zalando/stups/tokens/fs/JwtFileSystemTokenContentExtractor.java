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
package org.zalando.stups.tokens.fs;

import static java.lang.System.currentTimeMillis;
import static java.time.Instant.ofEpochSecond;
import static java.util.Base64.getDecoder;
import static java.util.Date.from;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Optional;

import org.zalando.stups.tokens.AccessToken;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtFileSystemTokenContentExtractor implements TokenContentExtractor {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public AccessToken extract(String token, String type) {
        return tryJwt(token, type).orElse(new AccessToken(token, type, -1L, null));
    }

    protected Optional<AccessToken> tryJwt(String token, String type) {
        try {
            if (isJwt(token)) {
                final byte[] decoded = getDecoder().decode(splitToken(token)[1]);
                final JsonNode node = om.readTree(decoded);
                long expiration = node.get("exp").asLong(-1L);
                long initialValidSeconds = expiration - MILLISECONDS.toSeconds(currentTimeMillis());
                return ofNullable(new AccessToken(token,type, initialValidSeconds, from(ofEpochSecond(expiration))));
            }
            return empty();
        } catch (Exception e) {
            return empty();
        }
    }

    protected boolean isJwt(String token) {
        return  splitToken(token).length == 3 ? true : false;
    }

    protected String[] splitToken(String token) {
        return token.split("\\.");
    }

}
