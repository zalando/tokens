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

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In this state the service will not be called until timeout reached.
 * 
 * @author jbellmann
 *
 */
class Open implements State {

    private static final Logger LOG = LoggerFactory.getLogger(Open.class);

    private long timeoutUntil = -1;
    private AtomicInteger multi;

    private final MCBConfig config;

    Open(MCBConfig config) {
        this(config, 1);
    }

    Open(MCBConfig config, int multi) {
        this.config = config;
        if (multi < this.config.getMaxMulti()) {
            this.multi = new AtomicInteger(multi);
        } else {
            this.multi = new AtomicInteger(config.getMaxMulti());
        }
        long sleepTime = this.multi.get() * this.config.getTimeout();
        logSwitch(sleepTime);
        this.timeoutUntil = System.currentTimeMillis() + config.getTimeUnit().toMillis(sleepTime);
    }

    private void logSwitch(long sleepTime) {
        LOG.warn("{} SWITCHED TO OPEN for {} {}", this.config.getName(), sleepTime,
                this.config.getTimeUnit().toString());
    }

    public int nextMulti() {
        return multi.incrementAndGet();
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void onError() {
        // we are open and do not expect any errors
    }

    @Override
    public void onSuccess() {
        // same here, no success expected
    }

    @Override
    public String getName() {
        return this.config.getName();
    }

    @Override
    public State switchState() {
        if (timeoutUntil < System.currentTimeMillis()) {
            LOG.info("{} SWITCH TO HALF_OPEN", this.config.getName());
            return new HalfOpen(config, this);
        } else {
            return this;
        }
    }

}
