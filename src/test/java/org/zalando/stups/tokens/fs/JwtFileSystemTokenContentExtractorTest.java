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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import org.zalando.stups.tokens.AccessToken;

public class JwtFileSystemTokenContentExtractorTest {

    private final String jwt = "987654321.eyJzdWIiOiJmOGQ2N3J0NS01YWM1LTQ5OGQtYWE2YS0wOTc0MTMwZGU3ZjUiLCAiaHR0cHM6Ly9leGFtcGxlLm9yZy9yZWFsbSI6ICJ1c2VycyIsICJodHRwczovL2V4YW1wbGUub3JnL3Rva2VuIiA6ICJCZWFyZXIiLCAiaHR0cHM6Ly9leGFtcGxlLm9yZy9tYW5hZ2VkLWlkIjogInVzZXIxIiwgImF1dGhfdGltZSI6IDE0OTYzMTM4NDEsICJpc3MiOiAiaHR0cHM6Ly9leGFtcGxlLm9yZyIsICJleHAiOiAxNDk2MzE3NDQxLCAiaWF0IjogMTQ5NjMxMzgzMX0=.1234567890";
    private final String token = UUID.randomUUID().toString();

    @Test
    public void extractContent() {
        JwtFileSystemTokenContentExtractor e = new JwtFileSystemTokenContentExtractor();
        AccessToken at = e.extract(jwt, "Bearer");
        assertThat(at).isNotNull();
        assertThat(at.getValidUntil()).isNotNull();
        assertThat(at.getValidUntil()).isBefore(new Date());
        assertThat(at.getInitialValidSeconds()).isLessThan(0);
    }

    @Test
    public void extractContentNonJwt() {
        JwtFileSystemTokenContentExtractor e = new JwtFileSystemTokenContentExtractor();
        AccessToken at = e.extract(token, "Bearer");
        assertThat(at).isNotNull();
        assertThat(at.getValidUntil()).isNull();
        assertThat(at.getInitialValidSeconds()).isLessThan(0);
    }
}
