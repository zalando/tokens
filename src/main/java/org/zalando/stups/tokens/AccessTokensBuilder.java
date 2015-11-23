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

import java.net.URI;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.config.RequestConfig;

public class AccessTokensBuilder {
    private final URI accessTokenUri;

    private ClientCredentialsProvider clientCredentialsProvider = null;
    private UserCredentialsProvider userCredentialsProvider = null;
    private int refreshPercentLeft = 40;
    private int warnPercentLeft = 20;
    
    // default here would -1 (undefined, system default)
    private int socketTimeout = 2000;
    
    // default here would -1 (undefined, system default)
    private int connectTimeout = 1000;
    
    // default here would -1 (undefined, system default)
    private int connectionRequestTimeout = 500;

    // default here would be 'true'
    // but javadoc mention ' ... cause up to 30 millisecond overhead per request and should be used only when appropriate'
    // so set default to false
    private boolean staleConnectionCheckEnabled = false;
    
    private int schedulingPeriod = 5;

    private final Set<AccessTokenConfiguration> accessTokenConfigurations = new HashSet<AccessTokenConfiguration>();

    private boolean locked = false;

    AccessTokensBuilder(final URI accessTokenUri) {
        this.accessTokenUri = accessTokenUri;
    }

    private void checkLock() {
        if (locked) {
            throw new IllegalStateException("configuration already done");
        }
    }

    private void checkNotNull(final String name, final Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }

    public AccessTokensBuilder usingClientCredentialsProvider(
            final ClientCredentialsProvider clientCredentialsProvider) {
        checkLock();
        checkNotNull("clientCredentialsProvider", clientCredentialsProvider);
        this.clientCredentialsProvider = clientCredentialsProvider;
        return this;
    }

    public AccessTokensBuilder usingUserCredentialsProvider(final UserCredentialsProvider userCredentialsProvider) {
        checkLock();
        checkNotNull("userCredentialsProvider", userCredentialsProvider);
        this.userCredentialsProvider = userCredentialsProvider;
        return this;
    }

    public AccessTokensBuilder refreshPercentLeft(final int refreshPercentLeft) {
        checkLock();
        this.refreshPercentLeft = refreshPercentLeft;
        return this;
    }

    public AccessTokensBuilder warnPercentLeft(final int warnPercentLeft) {
        checkLock();
        this.warnPercentLeft = warnPercentLeft;
        return this;
    }
    
    /**
     * 
     * @see RequestConfig#getSocketTimeout()
     * 
     * @param socketTimeout
     * @return
     */
    public AccessTokensBuilder socketTimeout(final int socketTimeout){
    	checkLock();
    	this.socketTimeout = socketTimeout;
    	return this;
    }
    
    /**
     * 
     * @see RequestConfig#getConnectTimeout()
     * 
     * @param connectTimeout
     * @return
     */
    public AccessTokensBuilder connectTimeout(final int connectTimeout){
    	checkLock();
    	this.connectTimeout = connectTimeout;
    	return this;
    }

    /**
     * 
     * @see RequestConfig#getConnectionRequestTimeout()
     * 
     * @param connectionRequestTimeout
     * @return
     */
    public AccessTokensBuilder connectionRequestTimeout(final int connectionRequestTimeout){
    	checkLock();
    	this.connectionRequestTimeout = connectionRequestTimeout;
    	return this;
    }
    
    /**
     * 
     * @see RequestConfig#isStaleConnectionCheckEnabled()
     * 
     * @param staleConnectionCheckEnabled
     * @return
     */
    public AccessTokensBuilder staleConnectionCheckEnabled(final boolean staleConnectionCheckEnabled){
    	checkLock();
    	this.staleConnectionCheckEnabled = staleConnectionCheckEnabled;
    	return this;
    }
    
    public AccessTokensBuilder schedulingPeriod(final int schedulingPeriod){
    	checkLock();
    	this.schedulingPeriod = schedulingPeriod;
    	return this;
    }

    public AccessTokenConfiguration manageToken(final Object tokenId) {
        checkLock();
        checkNotNull("tokenId", tokenId);

        final AccessTokenConfiguration config = new AccessTokenConfiguration(tokenId, this);
        accessTokenConfigurations.add(config);
        return config;
    }

    URI getAccessTokenUri() {
        return accessTokenUri;
    }

    ClientCredentialsProvider getClientCredentialsProvider() {
        return clientCredentialsProvider;
    }

    UserCredentialsProvider getUserCredentialsProvider() {
        return userCredentialsProvider;
    }

    int getRefreshPercentLeft() {
        return refreshPercentLeft;
    }

    int getWarnPercentLeft() {
        return warnPercentLeft;
    }

    public int getSocketTimeout() {
		return socketTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public int getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}

	public boolean isStaleConnectionCheckEnabled() {
		return staleConnectionCheckEnabled;
	}
	
	public int getSchedulingPeriod() {
		return schedulingPeriod;
	}

	Set<AccessTokenConfiguration> getAccessTokenConfigurations() {
        return Collections.unmodifiableSet(accessTokenConfigurations);
    }

    public AccessTokens start() {
        if (accessTokenConfigurations.size() == 0) {
            throw new IllegalArgumentException("no scopes defined");
        }

        locked = true;
        if (clientCredentialsProvider == null) {

            // use default
            clientCredentialsProvider = new JsonFileBackedClientCredentialsProvider();
        }

        if (userCredentialsProvider == null) {

            // use default
            userCredentialsProvider = new JsonFileBackedUserCredentialsProvider();
        }

        final AccessTokenRefresher refresher = getAccessTokenRefresher();
        refresher.start();
        return refresher;
    }

    protected AccessTokenRefresher getAccessTokenRefresher() {
        return new AccessTokenRefresher(this);
    }
}
