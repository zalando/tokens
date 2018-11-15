package org.zalando.stups.tokens;

import static java.lang.System.currentTimeMillis;
import static java.time.Instant.ofEpochSecond;
import static java.util.Base64.getDecoder;
import static java.util.Date.from;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtFileSystemTokenContentExtractor implements TokenContentExtractor {

    private final Logger logger = LoggerFactory.getLogger(JwtFileSystemTokenContentExtractor.class);

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public AccessToken extract(final String token, final String type) {
        return tryJwt(token, type).orElse(new AccessToken(token, type));
    }

    protected Optional<AccessToken> tryJwt(final String token, final String type) {
        try {
            if (isJwt(token)) {
                final byte[] decoded = getDecoder().decode(splitToken(token)[1]);
                final JsonNode node = om.readTree(decoded);
                final long expiration = node.get("exp").asLong(-1L);
                final long initialValidSeconds = expiration - MILLISECONDS.toSeconds(currentTimeMillis());
                return ofNullable(new AccessToken(token,type, initialValidSeconds, from(ofEpochSecond(expiration))));
            }
            return empty();
        } catch (final Exception e) {
            logger.warn(e.getMessage());
            return empty();
        }
    }

    protected boolean isJwt(final String token) {
        return splitToken(token).length == 3;
    }

    protected String[] splitToken(final String token) {
        return token.split("\\.");
    }

}
