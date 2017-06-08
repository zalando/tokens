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

import java.util.concurrent.atomic.AtomicLong;

/**
 * If 'closed' the service is available for usage.<br/>
 * Switches to 'open' if a threshold is greater or equal the configured value
 * {@link MCBConfig#getErrorThreshold()}.
 * 
 * 
 * @author jbellmann
 *
 */
class Closed implements State {

    private final AtomicLong errorCount = new AtomicLong(0);
    private final MCBConfig config;

    public Closed(MCBConfig config) {
        this.config = config;
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public void onError() {
        errorCount.incrementAndGet();
    }

    @Override
    public void onSuccess() {
        errorCount.set(0);
    }

    @Override
    public String getName() {
        return this.config.getName();
    }

    @Override
    public State switchState() {
        if (errorCount.get() >= config.getErrorThreshold()) {
            return new Open(config);
        } else {
            return this;
        }
    }

}
