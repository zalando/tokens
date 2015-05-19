package org.zalando.stups.tokens;

public class ClientCredentials {
    private final String id;
    private final String secret;

    public ClientCredentials(String id, String secret) {
        this.id = id;
        this.secret = secret;
    }

    public String getId() {
        return id;
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public String toString() {
        return "ClientCredentials{" +
                "id='" + id + '\'' +
                '}';
    }
}
