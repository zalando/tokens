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

import static org.zalando.stups.tokens.util.Objects.notNull;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zalando.stups.tokens.mcb.MCBConfig;

public class AccessTokensBuilder implements TokenRefresherConfiguration {
    private final URI accessTokenUri;
    private URI tokenInfoUri;

    private ClientCredentialsProvider clientCredentialsProvider = null;
    private UserCredentialsProvider userCredentialsProvider = null;
    private int refreshPercentLeft = 40;
    private int warnPercentLeft = 20;

    private final HttpConfig httpConfig = new HttpConfig();
    private final Set<AccessTokenConfiguration> accessTokenConfigurations = new HashSet<>();

    private boolean locked = false;
    private HttpProviderFactory httpProviderFactory;
    private int schedulingPeriod = 5;
    private ScheduledExecutorService executorService;

    private int tokenVerifierSchedulingPeriod = 5 * 60;
    private TokenVerifierProvider tokenVerifierProvider;

    private MCBConfig tokenRefresherMcbConfig = new MCBConfig.Builder().build();

    private MCBConfig tokenVerifierMcbConfig = new MCBConfig.Builder().withErrorThreshold(3).withMaxMulti(4)
            .withTimeout(10).withTimeUnit(TimeUnit.MINUTES).build();

    private MetricsListener metricsListener = new DebugLogMetricsListener();

    AccessTokensBuilder(final URI accessTokenUri) {
        this.accessTokenUri = notNull("accessTokenUri", accessTokenUri);
    }

    private void checkLock() {
        if (locked) {
            throw new IllegalStateException("configuration already done");
        }
    }

    public AccessTokensBuilder usingClientCredentialsProvider(
            final ClientCredentialsProvider clientCredentialsProvider) {
        checkLock();
        this.clientCredentialsProvider = notNull("clientCredentialsProvider", clientCredentialsProvider);
        return this;
    }

    public AccessTokensBuilder usingUserCredentialsProvider(final UserCredentialsProvider userCredentialsProvider) {
        checkLock();
        this.userCredentialsProvider = notNull("userCredentialsProvider", userCredentialsProvider);
        return this;
    }

    public AccessTokensBuilder usingHttpProviderFactory(final HttpProviderFactory factory) {
        checkLock();
        this.httpProviderFactory = notNull("httpProviderFactory", factory);
        return this;
    }

    public AccessTokensBuilder usingTokenVerifierProvider(TokenVerifierProvider tokenVerifierProvider) {
        checkLock();
        this.tokenVerifierProvider = notNull("tokenVerifierProvider", tokenVerifierProvider);
        return this;
    }

    public AccessTokensBuilder socketTimeout(final int socketTimeout) {
        checkLock();
        this.httpConfig.setSocketTimeout(socketTimeout);
        return this;
    }

    public AccessTokensBuilder existingExecutorService(final ScheduledExecutorService executorService) {
        checkLock();
        this.executorService = notNull("executorService",executorService);
        return this;
    }

    public AccessTokensBuilder connectTimeout(final int connectTimeout) {
        checkLock();
        this.httpConfig.setConnectTimeout(connectTimeout);
        return this;
    }

    public AccessTokensBuilder connectionRequestTimeout(final int connectionRequestTimeout) {
        checkLock();
        this.httpConfig.setConnectionRequestTimeout(connectionRequestTimeout);
        return this;
    }

    public AccessTokensBuilder staleConnectionCheckEnabled(final boolean staleConnectionCheckEnabled) {
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
        notNull("tokenId",tokenId);

        final AccessTokenConfiguration config = new AccessTokenConfiguration(tokenId, this);
        accessTokenConfigurations.add(config);
        return config;
    }

    public AccessTokensBuilder schedulingPeriod(final int schedulingPeriod) {
        checkLock();
        this.schedulingPeriod = schedulingPeriod;
        return this;
    }

    public AccessTokensBuilder tokenVerifierSchedulingPeriod(int tokenVerifierSchedulingPeriod) {
        checkLock();
        this.tokenVerifierSchedulingPeriod = tokenVerifierSchedulingPeriod;
        return this;
    }

    public AccessTokensBuilder tokenInfoUri(URI tokenInfoUri) {
        checkLock();
        this.tokenInfoUri = notNull("tokenInfoUri", tokenInfoUri);
        return this;
    }

    public AccessTokensBuilder tokenRefresherMcbConfig(MCBConfig config) {
        checkLock();
        this.tokenRefresherMcbConfig = notNull("tokenRefresherMcbConfig", config);
        return this;
    }

    public AccessTokensBuilder tokenVerifierMcbConfig(MCBConfig config) {
        checkLock();
        this.tokenVerifierMcbConfig = notNull("tokenVerifierMcbConfig", config);
        return this;
    }

    public AccessTokensBuilder metricsListener(MetricsListener metricsListener) {
        checkLock();
        this.metricsListener = notNull("metricsListener", metricsListener);
        return this;
    }

    @Override
    public int getSchedulingPeriod() {
        return schedulingPeriod;
    }

    @Override
    public URI getAccessTokenUri() {
        return accessTokenUri;
    }

    @Override
    public URI getTokenInfoUri() {
        return tokenInfoUri;
    }

    @Override
    public HttpProviderFactory getHttpProviderFactory() {
        return this.httpProviderFactory;
    }

    @Override
    public ClientCredentialsProvider getClientCredentialsProvider() {
        return clientCredentialsProvider;
    }

    @Override
    public UserCredentialsProvider getUserCredentialsProvider() {
        return userCredentialsProvider;
    }

    @Override
    public int getRefreshPercentLeft() {
        return refreshPercentLeft;
    }

    @Override
    public ScheduledExecutorService getExecutorService() {
        if (executorService == null) {
            return Executors.newSingleThreadScheduledExecutor();
        } else {
            return executorService;
        }
    }

    @Override
    public TokenVerifierProvider getTokenVerifierProvider() {
        if (tokenVerifierProvider == null) {
            return new CloseableTokenVerifierProvider();
        } else {
            return tokenVerifierProvider;
        }
    }

    @Override
    public int getWarnPercentLeft() {
        return warnPercentLeft;
    }

    @Override
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

        if (httpProviderFactory == null) {
            this.httpProviderFactory = new ClosableHttpProviderFactory();
        }

        final AccessTokenRefresher refresher = getAccessTokenRefresher();
        refresher.start();
        return refresher;
    }

    @Override
    public HttpConfig getHttpConfig() {
        return httpConfig;
    }

    protected AccessTokenRefresher getAccessTokenRefresher() {
        return new AccessTokenRefresher(this);
    }

    @Override
    public int getTokenVerifierSchedulingPeriod() {
        return tokenVerifierSchedulingPeriod;
    }

    @Override
    public MCBConfig getTokenRefresherMcbConfig() {
        return tokenRefresherMcbConfig;
    }

    @Override
    public MCBConfig getTokenVerifierMcbConfig() {
        return tokenVerifierMcbConfig;
    }

    @Override
    public MetricsListener getMetricsListener() {
        return metricsListener;
    }

}
