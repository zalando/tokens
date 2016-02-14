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
