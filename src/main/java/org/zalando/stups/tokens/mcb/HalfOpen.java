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

    HalfOpen(Open source) {
        this.source = source;
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
    public State switchState() {
        if (error.get()) {
            LOG.debug("SWITCH STATE TO OPEN");
            return new Open(source.nextMulti());
        } else {
            LOG.debug("SWITCH STATE TO CLOSED");
            return new Closed();
        }
    }

}
