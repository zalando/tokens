package org.zalando.stups.tokens;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom-{@link ThreadFactory} to be used by the
 * {@link ScheduledExecutorService} for {@link AccessTokenRefresher}.
 *
 */
public final class TokenRefresherThreadFactory implements ThreadFactory {
    private static final String THREAD_NAME_PREFIX = "token-refresher-";
    private final AtomicInteger counter = new AtomicInteger(0);
    private final boolean createDaemons;

    public TokenRefresherThreadFactory(boolean createDaemons) {
        this.createDaemons = createDaemons;
    }

    public TokenRefresherThreadFactory() {
        this(true);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, THREAD_NAME_PREFIX + counter.incrementAndGet());
        t.setDaemon(createDaemons);
        return t;
    }

}