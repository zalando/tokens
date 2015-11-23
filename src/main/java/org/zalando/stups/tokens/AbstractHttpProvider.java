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

import java.util.*;

public abstract class AbstractHttpProvider implements HttpProvider {
    protected String joinScopes(final Collection<Object> scopes) {
        final Iterator<Object> iter = scopes.iterator();

        final StringBuilder scope = new StringBuilder(iter.next().toString());
        while (iter.hasNext()) {
            scope.append(' ');
            scope.append(iter.next().toString());
        }

        return scope.toString();
    }

    protected Date calculateValidUntil(AccessTokenResponse accessTokenResponse) {
        return new Date(System.currentTimeMillis()
                + (accessTokenResponse.getExpiresInSeconds() * 1000));
    }

    protected Map<String, String> buildParameterMap(final AccessTokenConfiguration tokenConfig, UserCredentials userCredentials) {
        Map<String, String> nameValuePairs = new HashMap<>();
        String grantType = tokenConfig.getGrantType();
        nameValuePairs.put("grant_type", grantType);
        nameValuePairs.put("scope", joinScopes(tokenConfig.getScopes()));
        if ("password".equals(grantType)) {
            nameValuePairs.put("username", userCredentials.getUsername());
            nameValuePairs.put("password", userCredentials.getPassword());
        }
        return nameValuePairs;
    }
}
