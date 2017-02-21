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

/**
 * Use the <i>AccessTokensBuilder</i> obtained via
 * {@link Tokens#createAccessTokensWithUri(URI)} to build your configuration for
 * obtaining an {@link AccessToken} for different scopes / services via the
 * {@link AccessTokens#getAccessToken(Object)} or
 * {@link AccessTokens#get(Object)} methods on the instance returned after
 * invoking {@link AccessTokensBuilder#start()}.
 *
 * This class offers a Fluent Interface type of Builder. You can invoke any of
 * the methods on the initially retrieved instance until you call
 * {@link AccessTokensBuilder#start()}. Invoking any at any later point of time
 * will throw an {@link IllegalStateException}.
 *
 */
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
    private TimeUnit schedulingTimeUnit = TimeUnit.SECONDS;
    private ScheduledExecutorService executorService;

    private int tokenVerifierSchedulingPeriod = 5;
    private TimeUnit tokenVerifierSchedulingTimeUnit = TimeUnit.MINUTES;
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

    /**
     * Use the supplied implementation of {@link ClientCredentialsProvider} to
     * create the {@link ClientCredentials} which will be used to authenticate
     * the client when requesting a new access token.
     *
     * See https://tools.ietf.org/html/rfc6749#section-1.3.4 for further
     * information on the meaning of client credentials in the context of
     * OAuth2.0
     *
     * @param clientCredentialsProvider
     *            Your implementation of the {@link ClientCredentialsProvider}
     *            interface to use. See
     *            {@link JsonFileBackedClientCredentialsProvider} for a
     *            potential implementation
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied
     *         {@link ClientCredentialsProvider} set.
     */
    public AccessTokensBuilder usingClientCredentialsProvider(
            final ClientCredentialsProvider clientCredentialsProvider) {
        checkLock();
        this.clientCredentialsProvider = notNull("clientCredentialsProvider", clientCredentialsProvider);
        return this;
    }

    /**
     * Use the supplied implementation of {@link UserCredentialsProvider} to
     * create the {@link UserCredentials} which will be used to authenticate the
     * user when requesting a new access token.
     *
     * See https://tools.ietf.org/html/rfc6749#section-1.3.3 for further
     * information on the meaning of user credentials and how they can be used.
     *
     * @param userCredentialsProvider
     *            Your implementation of the {@link UserCredentialsProvider}
     *            interface to use. See
     *            {@link JsonFileBackedUserCredentialsProvider} for a potential
     *            implementation
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied
     *         {@link UserCredentialsProvider} set.
     */
    public AccessTokensBuilder usingUserCredentialsProvider(final UserCredentialsProvider userCredentialsProvider) {
        checkLock();
        this.userCredentialsProvider = notNull("userCredentialsProvider", userCredentialsProvider);
        return this;
    }

    /**
     * Use the supplied implementation of {@link HttpProviderFactory} to create
     * the {@link HttpProvider} which will be used for requesting new access
     * tokens.
     *
     * @param factory
     *            Your implementation of the {@link HttpProviderFactory} to use.
     *            See {@link ClosableHttpProviderFactory} for a potential
     *            implementation.
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied {@link HttpProviderFactory}
     *         set.
     */
    public AccessTokensBuilder usingHttpProviderFactory(final HttpProviderFactory factory) {
        checkLock();
        this.httpProviderFactory = notNull("httpProviderFactory", factory);
        return this;
    }

    /**
     * Use the supplied implementation of {@link TokenVerifierProvider} to
     * create the {@link TokenVerifier} to use for verifying access and refresh
     * tokens
     *
     * @param tokenVerifierProvider
     *            Your implementation of the {@link TokenVerifierProvider} to
     *            user. See {@link CloseableTokenVerifierProvider} for a
     *            potential implementation.
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied {@link TokenVerifierProvider}
     *         set.
     */
    public AccessTokensBuilder usingTokenVerifierProvider(TokenVerifierProvider tokenVerifierProvider) {
        checkLock();
        this.tokenVerifierProvider = notNull("tokenVerifierProvider", tokenVerifierProvider);
        return this;
    }

    /**
     * Change the socket timeout in milliseconds to be used for HTTP
     * connections. Default value is 2000.
     *
     * @param socketTimeout
     *            Your desired socket timeout in milliseconds
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied <i>socket timeout</i> set.
     */
    public AccessTokensBuilder socketTimeout(final int socketTimeout) {
        checkLock();
        this.httpConfig.setSocketTimeout(socketTimeout);
        return this;
    }

    /**
     * Use the supplied existing {@link ScheduledExecutorService} for all
     * scheduled operations done by the created {@link AccessTokens}
     * implementation instead of creating a new one.
     *
     * If none is supplied a new one will be created using
     * {@link Executors#newSingleThreadScheduledExecutor(java.util.concurrent.ThreadFactory)} upon start.
     *
     * @param executorService
     *            Your version of the {@link ScheduledExecutorService} to be
     *            used.
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied
     *         {@link ScheduledExecutorService} set.
     *
     * @see TokenRefresherThreadFactory
     * @see Executors#newSingleThreadExecutor(java.util.concurrent.ThreadFactory)
     */
    public AccessTokensBuilder existingExecutorService(final ScheduledExecutorService executorService) {
        checkLock();
        this.executorService = notNull("executorService", executorService);
        return this;
    }

    /**
     * Change the connect timeout in milliseconds to be used for HTTP
     * connections. Default value is 1000.
     *
     * @param connectTimeout
     *            Your desired connect timeout in milliseconds
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied <i>connect timeout</i> set.
     */
    public AccessTokensBuilder connectTimeout(final int connectTimeout) {
        checkLock();
        this.httpConfig.setConnectTimeout(connectTimeout);
        return this;
    }

    /**
     * Change the connection request timeout in milliseconds to be used for
     * requesting new HTTP connections from the connection manager. Default
     * value is 500.
     *
     * @param connectionRequestTimeout
     *            Your desired connection request timeout in milliseconds
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied <i>connection request
     *         timeout</i> set.
     */
    public AccessTokensBuilder connectionRequestTimeout(final int connectionRequestTimeout) {
        checkLock();
        this.httpConfig.setConnectionRequestTimeout(connectionRequestTimeout);
        return this;
    }

    /**
     * Change whether the stale connection check should be enabled or disabled
     * when requesting a HTTP connection from the connection manager. Default
     * behaviour is enabled.
     *
     * @param staleConnectionCheckEnabled
     *            True in case the stale connection check should be enabled
     *            false otherwise
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied <i>stale connection check
     *         value</i> set.
     */
    public AccessTokensBuilder staleConnectionCheckEnabled(final boolean staleConnectionCheckEnabled) {
        checkLock();
        this.httpConfig.setStaleConnectionCheckEnabled(staleConnectionCheckEnabled);
        return this;
    }

    /**
     * Set the threshold of the validity time left before the service tries to
     * refresh an access token with the authorization server. Default value is
     * 40.
     *
     * @param refreshPercentLeft
     *            The percentage of validity time left that triggers refreshing
     *            an access token
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied <i>refresh percentage left</i>
     *         set.
     */
    public AccessTokensBuilder refreshPercentLeft(final int refreshPercentLeft) {
        checkLock();
        this.refreshPercentLeft = refreshPercentLeft;
        return this;
    }

    /**
     * Set the threshold of the validity time left before the service issues a
     * warning. This value can be used to detect possible access token
     * refreshing problems e.g. Default value is 20.
     *
     * @param warnPercentLeft
     *            The percentage of validity time left that triggers a warning
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied <i>warn percentage left</i>
     *         set.
     */
    public AccessTokensBuilder warnPercentLeft(final int warnPercentLeft) {
        checkLock();
        this.warnPercentLeft = warnPercentLeft;
        return this;
    }

    /**
     * Configure a new access token configuration that should be managed by the
     * returned {@link AccessTokens} implementation. You can manage multiple
     * different tokens by invoking this method multiple times with different
     * values for <i>tokenId</i>.
     *
     * You can e.g. manage a read only token as well as a write token. It is
     * considered best practice to use as limited scopes as reasonable to
     * mitigate the security implications that arise by leaked access tokens.
     *
     * @param tokenId
     *            A unique id for this specific access token configuration. The
     *            supplied object must implement {@link Object#equals(Object)}
     *            in a way that it identifies the same value correctly. A
     *            straight forward version would be using a {@link String}
     *            value.
     * @return An instance of {@link AccessTokenConfiguration} which offers a
     *         fluent interface type of configuration for a single
     *         <i>tokenId</i>. Use the returned object to configure e.g the
     *         scopes for that specific token.
     */
    public AccessTokenConfiguration manageToken(final Object tokenId) {
        checkLock();
        notNull("tokenId", tokenId);

        final AccessTokenConfiguration config = new AccessTokenConfiguration(tokenId, this);
        accessTokenConfigurations.add(config);
        return config;
    }

    /**
     * Configure the amount of time between two runs of checking which existing
     * access tokens should be refreshed. This method must be used together with
     * {@link AccessTokensBuilder#schedulingTimeUnit(TimeUnit)} to define the
     * scheduling. The meaning of the value supplied here depends on the setting
     * for the {@link TimeUnit}. Default value is set to 5.
     *
     * @param schedulingPeriod
     *            The value for the <i>scheduling period</i> to use.
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied <i>scheduling period</i> set.
     */
    public AccessTokensBuilder schedulingPeriod(final int schedulingPeriod) {
        checkLock();
        this.schedulingPeriod = schedulingPeriod;
        return this;
    }

    /**
     * Configure the {@link TimeUnit} used together with the configured
     * <i>scheduling period</i> to use for checking which access tokens should
     * be refreshed. Default value is {@link TimeUnit#SECONDS}
     *
     * @param timeUnit
     *            The {@link TimeUnit} to use together with the configured
     *            <i>scheduling period</i>
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied {@link TimeUnit} set.
     */
    public AccessTokensBuilder schedulingTimeUnit(TimeUnit timeUnit) {
        checkLock();
        this.schedulingTimeUnit = notNull("schedulingTimeUnit", timeUnit);
        return this;
    }

    /**
     * Configure the amount of time that should pass between two run of the
     * {@link TokenVerifier}. The exact meaning of this value depends on the
     * value set by
     * {@link AccessTokensBuilder#tokenVerifierSchedulingTimeUnit(TimeUnit)}.
     * Default value is 5.
     *
     * @param tokenVerifierSchedulingPeriod
     *            The value for the <i>token verification scheduling period</i>
     *            to use.
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied <i>token verification
     *         scheduling period</i> set.
     */
    public AccessTokensBuilder tokenVerifierSchedulingPeriod(int tokenVerifierSchedulingPeriod) {
        checkLock();
        this.tokenVerifierSchedulingPeriod = tokenVerifierSchedulingPeriod;
        return this;
    }

    /**
     * Configure the {@link TimeUnit} used together with the configured <i>token
     * verification scheduling period</i> to use for verifying existing access
     * tokens. Default value is {@link TimeUnit#SECONDS}
     *
     * @param timeUnit
     *            The {@link TimeUnit} to use together with the configured
     *            <i>token verification scheduling period</i>
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied {@link TimeUnit} set.
     */
    public AccessTokensBuilder tokenVerifierSchedulingTimeUnit(TimeUnit timeUnit) {
        checkLock();
        this.tokenVerifierSchedulingTimeUnit = notNull("tokenVerifierSchedulingTimeUnit", timeUnit);
        return this;
    }

    /**
     * Configure the <i>token info URI</i> to be used by the
     * {@link TokenVerifier} to verify existing access tokens.
     *
     * @param tokenInfoUri
     *            The <i>token info URI</i> to be used by the
     *            {@link TokenVerifier}
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied <i>token info URI</i> set.
     */
    public AccessTokensBuilder tokenInfoUri(URI tokenInfoUri) {
        checkLock();
        this.tokenInfoUri = notNull("tokenInfoUri", tokenInfoUri);
        return this;
    }

    /**
     * Configure the configuration for the circuit breaker to be used for
     * refreshing access tokens.
     *
     * @param config
     *            The {@link MCBConfig} that should be used for refreshing
     *            access tokens.
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied {@link MCBConfig} set.
     */
    public AccessTokensBuilder tokenRefresherMcbConfig(MCBConfig config) {
        checkLock();
        this.tokenRefresherMcbConfig = notNull("tokenRefresherMcbConfig", config);
        return this;
    }

    /**
     * Configure the configuration for the circuit breaker to be used for
     * verifying access tokens.
     *
     * @param config
     *            The {@link MCBConfig} that should be used for verifying access
     *            tokens.
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied {@link MCBConfig} set.
     */
    public AccessTokensBuilder tokenVerifierMcbConfig(MCBConfig config) {
        checkLock();
        this.tokenVerifierMcbConfig = notNull("tokenVerifierMcbConfig", config);
        return this;
    }

    /**
     * Configure the {@link MetricsListener} to be used by the created
     * {@link AccessTokens} implementation for reporting execution times.
     *
     * @param metricsListener
     *            The {@link MetricsListener} implementation to be used. See
     *            {@link DebugLogMetricsListener} for a potential implementation
     *            of this interface.
     * @return The same {@link AccessTokensBuilder} instance this method has
     *         been called upon with the supplied {@link MetricsListener} set.
     */
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
            return Executors.newSingleThreadScheduledExecutor(new TokenRefresherThreadFactory());
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

    /**
     * Create the {@link AccessTokens} instance along with any required
     * additional components. After this method has been invoked this
     * {@link AccessTokensBuilder} is in locked state and all further
     * invocations of any of the configuration methods will throw an
     * {@link IllegalStateException}.
     *
     * Invoking this method will create and start instances of
     * {@link AccessTokenRefresher} as well as {@link AccessTokenRefresher} as
     * configured previously using the {@link ScheduledExecutorService} as
     * configured.
     *
     * In case no {@link ClientCredentialsProvider} has been configured an
     * instance of {@link JsonFileBackedClientCredentialsProvider} is used.
     *
     * In case no {@link UserCredentialsProvider} has been configured an
     * instance of {@link JsonFileBackedUserCredentialsProvider} is used.
     *
     * In case no {@link HttpProviderFactory} has been configured an instance of
     * {@link ClosableHttpProviderFactory} is used.
     *
     * @return The {@link AccessTokens} instance built from the previously
     *         supplied configuration. Use it to e.g. get an {@link AccessToken}
     *         for any of your configured <i>tokenIds</i>.
     */
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

    @Override
    public TimeUnit getSchedulingTimeUnit() {
        return schedulingTimeUnit;
    }

    @Override
    public TimeUnit getTokenVerifierSchedulingTimeUnit() {
        return tokenVerifierSchedulingTimeUnit;
    }
}
