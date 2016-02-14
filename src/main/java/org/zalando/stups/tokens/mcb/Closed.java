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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If 'closed' the service is available for usage.<br/>
 * Switches to 'open' if a threshold is greater or equal 5.
 * 
 * @author jbellmann
 *
 */
class Closed implements State {

    private static final Logger LOG = LoggerFactory.getLogger(Closed.class);

    final AtomicLong errorCount = new AtomicLong(0);

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
        if (errorCount.get() > 0) {
            errorCount.decrementAndGet();
        }
    }

    @Override
    public State switchState() {
        if (errorCount.get() >= 5) {
            LOG.debug("SWITCH TO OPEN");
            return new Open();
        } else {
            return this;
        }
    }

}
