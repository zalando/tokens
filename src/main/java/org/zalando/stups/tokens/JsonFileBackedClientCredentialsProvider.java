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
