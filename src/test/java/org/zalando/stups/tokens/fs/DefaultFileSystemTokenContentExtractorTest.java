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

import java.util.UUID;

import org.junit.Test;
import org.zalando.stups.tokens.AccessToken;

public class DefaultFileSystemTokenContentExtractorTest {

    private DefaultFileSystemTokenContentExtractor extractor = new DefaultFileSystemTokenContentExtractor();

    @Test
    public void testDefaultExtractor() {
        String token = UUID.randomUUID().toString();
        AccessToken at = extractor.extract(token, "Bearer");
        assertThat(at).isNotNull();
        assertThat(at.getToken()).isNotBlank();
        assertThat(at.getType()).isEqualTo("Bearer");
        assertThat(at.getToken()).isEqualTo(token);
    }

}
