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
package org.zalando.stups.tokens.mcb;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Configuration for MCB.
 * 
 * @author jbellmann
 *
 */
public class MCBConfig {

    private final int errorThreshold;
    private final int timeout;
    private final int maxMulti;
    private final TimeUnit timeUnit;
    private final String name;

    private MCBConfig(int errorThreshold, int timeout, int maxMulti, TimeUnit timeUnit, String name) {
        this.errorThreshold = errorThreshold;
        this.timeout = timeout;
        this.maxMulti = maxMulti;
        this.timeUnit = timeUnit;
        this.name = name;
    }

    /**
     * How many errors before MCB switches to {@link Open}-state.
     * 
     * @return 5
     */
    public int getErrorThreshold() {
        return errorThreshold;
    }

    /**
     * Timeout base for {@link Open}.
     * 
     * @return 30
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * The max multiplier for the timeout in {@link Open}-state.
     * 
     * @return 40
     */
    public int getMaxMulti() {
        return maxMulti;
    }

    /**
     * TimeUnit for {@link #getTimeout()}.
     * 
     * @return TimeUnit.SECONDS
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Name for the breaker.
     * 
     * @return name the brokers name
     */
    public String getName() {
        return name;
    }

    public static class Builder {
        private static final AtomicLong nameCounter = new AtomicLong(0);
        private int threshold = 5;
        private int timeout = 30;
        private int maxMulti = 40;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
        private String name = "MCB-" + nameCounter.getAndIncrement();

        public Builder withErrorThreshold(int errorThreshold){
            this.threshold = errorThreshold;
            return this;
        }

        public Builder withTimeout(int timeout){
            this.timeout = timeout;
            return this;
        }

        public Builder withMaxMulti(int maxMulti) {
            this.maxMulti = maxMulti;
            return this;
        }

        public Builder withTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public MCBConfig build() {
            return new MCBConfig(threshold, timeout, maxMulti, timeUnit, name);
        }

    }

}
