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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.tokens.ClientCredentials;

public class ClientCredentialsHandlerTest {

    private Map<String, ClientCredentials> clientCredentials;

    @Before
    public void setup() {
        System.getProperties().put("CREDENTIALS_DIR", "k8s");
        clientCredentials = new ConcurrentHashMap<>();
    }

    @After
    public void tearDown() {
        System.getProperties().remove("CREDENTIALS_DIR");
    }

    @Test
    public void testClientCredentialsHandler() {
        FilesystemReader<?> reader = new ClientCredentialsHandler(clientCredentials).getFilesystemReader();
        reader.run();
        Assertions.assertThat(clientCredentials).isNotEmpty();
        Assertions.assertThat(clientCredentials).containsKey("kio");
    }

}
