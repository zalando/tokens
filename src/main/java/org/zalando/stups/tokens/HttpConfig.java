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
package org.zalando.stups.tokens;

import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;

/**
 * Configurations for {@link HttpProvider} used by {@link HttpProviderFactory}.
 * 
 * @author jbellmann
 *
 */
public class HttpConfig {

    public static final int DEFAULT_ABORT_TIMEOUT_VALUE = 10;
    public static final TimeUnit DEFAULT_ABORT_TIMEOUT_TIMEUNIT = TimeUnit.SECONDS;

    private int socketTimeout = 2000;

    private int connectTimeout = 1000;

    private int connectionRequestTimeout = 500;

    private boolean staleConnectionCheckEnabled = true;

    private int abortTimeoutValue = DEFAULT_ABORT_TIMEOUT_VALUE;

    private TimeUnit abortTimeoutTimeunit = DEFAULT_ABORT_TIMEOUT_TIMEUNIT;

    /**
     * @see RequestConfig#getSocketTimeout()
     * 
     * @return socket_timeout in milliseconds, defaults to 2000
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * @see RequestConfig#getConnectTimeout()
     * 
     * @return timeout in milliseconds until a connection is established,
     *         defaults to 1000
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * @see RequestConfig#getConnectionRequestTimeout()
     * 
     * @return timeout in milliseconds used when requesting a connection from
     *         the connection manager, defaults to 500
     */
    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    /**
     * @see RequestConfig#isStaleConnectionCheckEnabled()
     * 
     * @return true if stale connection check is enabled, defaults to true
     */
    public boolean isStaleConnectionCheckEnabled() {
        return staleConnectionCheckEnabled;
    }

    public void setStaleConnectionCheckEnabled(boolean staleConnectionCheckEnabled) {
        this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
    }

    public int getAbortTimeoutValue() {
        return abortTimeoutValue;
    }

    public void setAbortTimeoutValue(int abortTimeoutValue) {
        this.abortTimeoutValue = abortTimeoutValue;
    }

    public TimeUnit getAbortTimeoutTimeunit() {
        return abortTimeoutTimeunit;
    }

    public void setAbortTimeoutTimeunit(TimeUnit abortTimeoutTimeunit) {
        this.abortTimeoutTimeunit = abortTimeoutTimeunit;
    }

}
