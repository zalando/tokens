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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TokenRefresherThreadFactoryTest {

    @Test
    public void testDaemonThreads() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        ScheduledExecutorService es = Executors.newScheduledThreadPool(1, new TokenRefresherThreadFactory(true));
        es.scheduleWithFixedDelay(new AssertionsRunnable(latch, new Expected("token-refresher-", true)), 1, 1,
                TimeUnit.SECONDS);
        latch.await();
    }

    @Test
    public void testDaemonThreadsByDefault() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        ScheduledExecutorService es = Executors.newScheduledThreadPool(1, new TokenRefresherThreadFactory());
        es.scheduleWithFixedDelay(new AssertionsRunnable(latch, new Expected("token-refresher-", true)), 1, 1,
                TimeUnit.SECONDS);
        latch.await();
    }

    @Test
    public void testNonDaemonThreads() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        ScheduledExecutorService es = Executors.newScheduledThreadPool(1, new TokenRefresherThreadFactory(false));
        es.scheduleWithFixedDelay(new AssertionsRunnable(latch, new Expected("token-refresher-", false)), 1, 1,
                TimeUnit.SECONDS);
        latch.await();
    }

    static class Expected {
        private final String prefix;
        private final boolean daemon;

        public Expected(String prefix, boolean daemon) {
            this.prefix = prefix;
            this.daemon = daemon;
        }
    }

    static class AssertionsRunnable implements Runnable {

        private final CountDownLatch latch;
        private final Expected expected;

        public AssertionsRunnable(CountDownLatch latch, Expected expected) {
            this.latch = latch;
            this.expected = expected;
        }

        @Override
        public void run() {
            Assertions.assertThat(Thread.currentThread().getName().startsWith(expected.prefix));
            Assertions.assertThat(Thread.currentThread().isDaemon()).isEqualTo(expected.daemon);
            latch.countDown();
        }

    }
}
