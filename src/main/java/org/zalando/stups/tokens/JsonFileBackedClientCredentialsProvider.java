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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class JsonFileBackedClientCredentialsProvider extends AbstractJsonFileBackedCredentialsProvider
        implements ClientCredentialsProvider {

    public JsonFileBackedClientCredentialsProvider() {
        super("client.json");
    }

    public JsonFileBackedClientCredentialsProvider(final File file) {
        super(file);
    }

    @Override
    public ClientCredentials get() {
        return read(JsonBackedClientCredentials.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonBackedClientCredentials implements ClientCredentials {
        @JsonProperty("client_id")
        private String id;

        @JsonProperty("client_secret")
        private String secret;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getSecret() {
            return secret;
        }
    }
}
