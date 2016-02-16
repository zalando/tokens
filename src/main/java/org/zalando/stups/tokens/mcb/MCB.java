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

/**
 * Minimal Circuit Breaker.
 * 
 * @author jbellmann
 *
 */
public class MCB {

    private State state;

    /**
     * Uses defaults from {@link MCBConfig}.
     */
    public MCB() {
        this(new MCBConfig.Builder().build());
    }

    /**
     * Create one with a custom-config.
     * 
     * @param config
     */
    public MCB(MCBConfig config) {
        this.state = new Closed(config);
    }

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
