package org.zalando.stups.tokens;

public interface ClientCredentialsProvider {
    ClientCredentials get() throws CredentialsUnavailableException;
}
