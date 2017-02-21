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