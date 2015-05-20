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
