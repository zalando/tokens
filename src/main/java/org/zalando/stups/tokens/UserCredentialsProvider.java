package org.zalando.stups.tokens;

public interface UserCredentialsProvider {
    UserCredentials get() throws CredentialsUnavailableException;
}
