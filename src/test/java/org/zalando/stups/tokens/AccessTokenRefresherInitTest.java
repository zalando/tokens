package org.zalando.stups.tokens;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

public class AccessTokenRefresherInitTest {

    @Test
    public void testInitializeFromEnvironment() {
        System.setProperty("OAUTH2_ACCESS_TOKENS", "kio=1234567890,pierone=987654321");

        AccessTokenRefresher refresher = new AccessTokenRefresher(config(30, 10));
        refresher.initializeFixedTokensFromEnvironment();
        Assert.assertEquals(refresher.get("kio"), "1234567890");
        Assert.assertEquals(refresher.get("pierone"), "987654321");
    }

    @Test(expected = AccessTokenUnavailableException.class)
    public void testInitializeFromEnvironmentFailure() {
        System.setProperty("OAUTH2_ACCESS_TOKENS", "kio=,pierone=987654321");

        AccessTokenRefresher refresher = new AccessTokenRefresher(config(30, 10));
        refresher.initializeFixedTokensFromEnvironment();

        // this works
        refresher.get("pierone");

        // this should fail
        refresher.get("kio");

    }

    private static AccessTokensBuilder config(final int refreshPercentLeft, final int warnPercentLeft) {
        try {
            return Tokens.createAccessTokensWithUri(new URI("http://localhost")).refreshPercentLeft(refreshPercentLeft)
                         .warnPercentLeft(warnPercentLeft);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
