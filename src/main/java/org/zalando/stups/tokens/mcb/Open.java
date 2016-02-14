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
