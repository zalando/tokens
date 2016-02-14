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
    
    public static final int TIMEOUT = 30000;
    private static final int MAX_MULTI = 40;

    private long timeoutUntil = -1;
    private AtomicInteger multi;

    Open() {
        this(1);
    }

    Open(int multi) {
        if (multi < MAX_MULTI) {
            this.multi = new AtomicInteger(multi);
        } else {
            this.multi = new AtomicInteger(MAX_MULTI);
        }
        this.timeoutUntil = System.currentTimeMillis() + this.multi.get() * TIMEOUT;
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
        // same here, no errors expected
    }

    @Override
    public State switchState() {
        if (timeoutUntil < System.currentTimeMillis()) {
            LOG.debug("SWITCH STATE TO HALF_OPEN");
            return new HalfOpen(this);
        } else {
            return this;
        }
    }

}
