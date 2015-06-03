/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
