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

public class AccessToken {
    private final String token;
    private final String type;
    private final long initialValidSeconds;
    private final Date validUntil;

    public AccessToken(final String token, final String type, final long initialValidSeconds, final Date validUntil) {
        this.token = token;
        this.type = type;
        this.initialValidSeconds = initialValidSeconds;
        this.validUntil = validUntil;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public long getInitialValidSeconds() {
        return initialValidSeconds;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public boolean isExpired() {
        return validUntil != null && validUntil.before(new Date());
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
