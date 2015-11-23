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

public class AccessTokensBuilder implements TokenRefresherConfiguration {
    private final URI accessTokenUri;

    private ClientCredentialsProvider clientCredentialsProvider = null;
    private UserCredentialsProvider userCredentialsProvider = null;
    private int refreshPercentLeft = 40;
    private int warnPercentLeft = 20;

    private final HttpConfig httpConfig = new HttpConfig();
    private final Set<AccessTokenConfiguration> accessTokenConfigurations = new HashSet<AccessTokenConfiguration>();

    private boolean locked = false;
    private HttpProviderFactory httpProviderFactory;
    private int schedulingPeriod = 5;

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

    public AccessTokensBuilder usingHttpProviderFactory(HttpProviderFactory factory) {
        checkLock();
        this.httpProviderFactory = factory;
        return this;
    }

    public AccessTokensBuilder socketTimeout(final int socketTimeout){
        checkLock();
        this.httpConfig.setSocketTimeout(socketTimeout);
        return this;
    }

    public AccessTokensBuilder connectTimeout(final int connectTimeout){
        checkLock();
        this.httpConfig.setConnectTimeout(connectTimeout);
        return this;
    }

    public AccessTokensBuilder connectionRequestTimeout(final int connectionRequestTimeout){
        checkLock();
        this.httpConfig.setConnectionRequestTimeout(connectionRequestTimeout);
        return this;
    }

    public AccessTokensBuilder staleConnectionCheckEnabled(final boolean staleConnectionCheckEnabled){
        checkLock();
        this.httpConfig.setStaleConnectionCheckEnabled(staleConnectionCheckEnabled);
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

    public AccessTokenConfiguration manageToken(final Object tokenId) {
        checkLock();
        checkNotNull("tokenId", tokenId);

        final AccessTokenConfiguration config = new AccessTokenConfiguration(tokenId, this);
        accessTokenConfigurations.add(config);
        return config;
    }

    public AccessTokensBuilder schedulingPeriod(final int schedulingPeriod){
        checkLock();
        this.schedulingPeriod = schedulingPeriod;
        return this;
    }

    public int getSchedulingPeriod() {
        return schedulingPeriod;
    }

    public URI getAccessTokenUri() {
        return accessTokenUri;
    }

    @Override
    public HttpProviderFactory getHttpProviderFactory() {
        return this.httpProviderFactory;
    }

    public ClientCredentialsProvider getClientCredentialsProvider() {
        return clientCredentialsProvider;
    }

    public UserCredentialsProvider getUserCredentialsProvider() {
        return userCredentialsProvider;
    }

    public int getRefreshPercentLeft() {
        return refreshPercentLeft;
    }

    public int getWarnPercentLeft() {
        return warnPercentLeft;
    }

    public Set<AccessTokenConfiguration> getAccessTokenConfigurations() {
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
        if(httpProviderFactory == null) {
            this.httpProviderFactory = new ClosableHttpProviderFactory();
        }

        final AccessTokenRefresher refresher = getAccessTokenRefresher();
        refresher.start();
        return refresher;
    }

    public HttpConfig getHttpConfig() {
        return httpConfig;
    }

    protected AccessTokenRefresher getAccessTokenRefresher() {
        return new AccessTokenRefresher(this);
    }
}
