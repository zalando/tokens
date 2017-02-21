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
