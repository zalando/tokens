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

public class JsonFileBackedUserCredentialsProvider extends AbstractJsonFileBackedCredentialsProvider
        implements UserCredentialsProvider {

    public JsonFileBackedUserCredentialsProvider() {
        super("user.json");
    }

    public JsonFileBackedUserCredentialsProvider(final File file) {
        super(file);
    }

    @Override
    public UserCredentials get() {
        return read(JsonBackedUserCredentials.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonBackedUserCredentials implements UserCredentials {
        @JsonProperty("application_username")
        private String username;

        @JsonProperty("application_password")
        private String password;

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }
    }
}
