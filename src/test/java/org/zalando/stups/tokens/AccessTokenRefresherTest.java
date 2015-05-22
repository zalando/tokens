package org.zalando.stups.tokens;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

public class AccessTokenRefresherTest {

    private static AccessToken token(int secondsLeft, int secondsValid) {
        return new AccessToken("foo", "bar", secondsValid,
                new Date(System.currentTimeMillis() + (secondsLeft * 1000)));
    }

    private static AccessTokensBuilder config(int refreshPercentLeft, int warnPercentLeft) {
        try {
            return Tokens.createAccessTokensWithUri(new URI("http://localhost"))
                    .refreshPercentLeft(refreshPercentLeft)
                    .warnPercentLeft(warnPercentLeft);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean approx(int value, int expected, int variance) {
        if ((value >= expected - variance) && (value <= expected + variance)) {
            return true;
        } else {
            throw new IllegalStateException("value " + value + " is not in bounds of " + expected + " +/- " + variance);
        }
    }

    @Test
    public void testPercentCalculation() {
        Assert.assertTrue("0%", approx(AccessTokenRefresher.percentLeft(token(100, 100)), 100, 10));
        Assert.assertTrue("50%", approx(AccessTokenRefresher.percentLeft(token(50, 100)), 50, 10));
        Assert.assertTrue("90%", approx(AccessTokenRefresher.percentLeft(token(10, 100)), 10, 10));
        Assert.assertTrue("100%", approx(AccessTokenRefresher.percentLeft(token(0, 100)), 0, 10));
    }

    @Test
    public void testRefreshTiming() {
        Assert.assertFalse("100%", AccessTokenRefresher.shouldRefresh(token(100, 100), config(50, 20)));
        Assert.assertTrue("50%", AccessTokenRefresher.shouldRefresh(token(50, 100), config(50, 20)));
        Assert.assertTrue("10%", AccessTokenRefresher.shouldRefresh(token(10, 100), config(50, 20)));
    }

    @Test
    public void testWarnTiming() {
        Assert.assertFalse("100%", AccessTokenRefresher.shouldWarn(token(100, 100), config(50, 20)));
        Assert.assertFalse("50%", AccessTokenRefresher.shouldWarn(token(50, 100), config(50, 20)));
        Assert.assertTrue("10%", AccessTokenRefresher.shouldWarn(token(10, 100), config(50, 20)));
    }
}
