package org.zalando.stups.tokens.mcb;

public interface State {

    void onError();

    void onSuccess();

    boolean isClosed();

    State switchState();

}
