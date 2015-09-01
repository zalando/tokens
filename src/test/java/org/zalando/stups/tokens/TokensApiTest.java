package org.zalando.stups.tokens;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

/**
 * Testing whether the API actually provides the functionality we expose in the README
 */
public class TokensApiTest {

    @Test
    public void testExampleUsage() throws URISyntaxException {
        AccessTokens tokens = Tokens.createAccessTokensWithUri(new URI("https://example.com/access_tokens"))
                .manageToken("exampleRW")
                .addScope("read")
                .addScope("write")
                .done()
                .manageToken("exampleRO")
                .addScope("read")
                .done()
                .start();
    }

    @Test
    public void testDynamicUsage() throws URISyntaxException {
        AccessTokens tokens = Tokens.createAccessTokensWithUri(new URI("https://example.com/access_tokens"))
                .manageToken("a")
                .addScope(() -> "x")
                .done()
                .manageToken("b")
                .addScopes(HashSet::new)
                .done()
                .start();
    }

}
