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

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Follows after 'open' and switches back to open if the next service invocation
 * throws an error. Or switches to 'closed' if the service invocation succeeded.
 * 
 * @author jbellmann
 *
 */
class HalfOpen implements State {

    private static final Logger LOG = LoggerFactory.getLogger(HalfOpen.class);

    private final AtomicBoolean error = new AtomicBoolean(false);

    private final Open source;
    private final MCBConfig config;

    HalfOpen(MCBConfig config, Open source) {
        this.source = source;
        this.config = config;
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public void onError() {
        error.set(true);
    }

    @Override
    public void onSuccess() {
        // we only need one
    }

    @Override
    public String getName() {
        return this.config.getName();
    }

    @Override
    public State switchState() {
        if (error.get()) {
            return new Open(config, source.nextMulti());
        } else {
            LOG.info("{} SWITCH TO CLOSED", this.config.getName());
            return new Closed(config);
        }
    }

}
