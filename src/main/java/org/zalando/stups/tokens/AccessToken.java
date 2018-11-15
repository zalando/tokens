package org.zalando.stups.tokens;

import lombok.Value;

import java.time.Instant;

/**
 * "Model" class for an access token that will include some additional information about the token
 * beside just being a {@link String} value that can be used as a <i>Bearer</i> token with OAuth2.0
 */
@Value
public class AccessToken {
    
    String token;
    String type;
    Instant createdAt = Instant.now();

}
