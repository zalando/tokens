package org.zalando.stups.tokens.mcb;

/**
 * Minimal Circuit Breaker.
 * 
 * @author jbellmann
 *
 */
public class MCB {

    private State state = new Closed();

    public void onError() {
        this.state.onError();

    }

    public void onSuccess() {
        this.state.onSuccess();
    }

    public boolean isClosed() {
        this.state = state.switchState();
        return this.state.isClosed();
    }

}
