package org.zalando.stups.tokens.mcb;

import java.util.concurrent.TimeUnit;

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

    private MCBConfig(int errorThreshold, int timeout, int maxMulti, TimeUnit timeUnit) {
        this.errorThreshold = errorThreshold;
        this.timeout = timeout;
        this.maxMulti = maxMulti;
        this.timeUnit = timeUnit;
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

    public static class Builder {
        private int threshold = 5;
        private int timeout = 30;
        private int maxMulti = 40;
        private TimeUnit timeUnit = TimeUnit.SECONDS;

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

        public MCBConfig build() {
            return new MCBConfig(threshold, timeout, maxMulti, timeUnit);
        }

    }

}
