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

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.tokens.Secret;

public class SecretsHandlerTest {

    private Map<String, Secret> secrets = new HashMap<>();

    @Before
    public void setup() {
        System.getProperties().put("CREDENTIALS_DIR", "k8s");
    }

    @After
    public void tearDown() {
        System.getProperties().remove("CREDENTIALS_DIR");
    }

    @Test
    public void testAuthorizationHandler() {
        FilesystemReader<?> reader = new SecretsHandler(secrets).getFilesystemReader();
        reader.run();
        Assertions.assertThat(secrets).isNotEmpty();
        Assertions.assertThat(secrets).containsKeys("mybasic","myfirst");

    }

}
