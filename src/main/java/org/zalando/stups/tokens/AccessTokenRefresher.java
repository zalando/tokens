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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class AccessTokenRefresher implements AccessTokens, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AccessTokenRefresher.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final long ONE_YEAR_SECONDS = 3600*24*365;
    private static final String FIXED_TOKENS_ENV_VAR = "OAUTH2_ACCESS_TOKENS";

    private final AccessTokensBuilder configuration;
    private final ScheduledExecutorService scheduler;

    private ConcurrentHashMap<Object, AccessToken> accessTokens = new ConcurrentHashMap<Object, AccessToken>();

    public AccessTokenRefresher(final AccessTokensBuilder configuration) {
        this.configuration = configuration;
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    void initializeFixedTokensFromEnvironment() {
        final String csv = System.getenv(FIXED_TOKENS_ENV_VAR);
        if (csv != null) {
            LOG.info("Initializing fixed access tokens from {} environment variable..", FIXED_TOKENS_ENV_VAR);
            final String[] tokens = csv.split(",");
            final long expiresInSeconds = ONE_YEAR_SECONDS;
            final Date validUntil = new Date(System.currentTimeMillis() + (expiresInSeconds * 1000));
            for (String token : tokens) {
                final String[] keyValue = token.split("=");
                if (keyValue.length == 2) {
                    LOG.info("Using fixed access token {}..", keyValue[0]);
                    accessTokens.put(keyValue[0], new AccessToken(keyValue[1], "fixed", expiresInSeconds, validUntil));
                }
            }
        }
    }

    void start() {
        initializeFixedTokensFromEnvironment();
        LOG.info("Starting to refresh tokens regularly...");
        scheduler.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
    }

    static int percentLeft(final AccessToken token) {
        final long now = System.currentTimeMillis();
        final long validUntil = token.getValidUntil().getTime();
        final long hundredPercentSeconds = token.getInitialValidSeconds();
        final long secondsLeft = (validUntil - now) / 1000;
        return (int) ((double)secondsLeft / (double)hundredPercentSeconds * (double)100);
    }

    static boolean shouldRefresh(final AccessToken token, AccessTokensBuilder configuration) {
        return percentLeft(token) <= configuration.getRefreshPercentLeft();
    }

    static boolean shouldWarn(final AccessToken token, AccessTokensBuilder configuration) {
        return percentLeft(token) <= configuration.getWarnPercentLeft();
    }

    @Override
    public void run() {
        try {
            for (final AccessTokenConfiguration tokenConfig : configuration.getAccessTokenConfigurations()) {
                final AccessToken oldToken = accessTokens.get(tokenConfig.getTokenId());
                // TODO optionally check with tokeninfo endpoint regularly (every x% of time)
                if (oldToken == null || shouldRefresh(oldToken, configuration)) {
                    try {
                        LOG.trace("Refreshing access token {}...", tokenConfig.getTokenId());
                        final AccessToken newToken = createToken(tokenConfig);
                        accessTokens.put(tokenConfig.getTokenId(), newToken);
                        LOG.info("Refreshed access token {}.", tokenConfig.getTokenId());
                    } catch (final Throwable t) {
                        if (oldToken == null || shouldWarn(oldToken, configuration)) {
                            LOG.warn("Cannot refresh access token {} because {}.", tokenConfig.getTokenId(), t);
                        } else {
                            LOG.info("Cannot refresh access token {} because {}.", tokenConfig.getTokenId(), t);
                        }
                    }
                }

            }
        } catch (final Throwable t) {
            LOG.error("Unexpected problem during token refresh run!", t);
        }
    }

    private static String joinScopes(final Collection<Object> scopes) {
        final Iterator<Object> iter = scopes.iterator();

        final StringBuilder scope = new StringBuilder(iter.next().toString());
        while (iter.hasNext()) {
            scope.append(' ');
            scope.append(iter.next().toString());
        }

        return scope.toString();
    }

    private AccessToken createToken(final AccessTokenConfiguration tokenConfig) {
        try {
            // collect credentials
            final ClientCredentials clientCredentials = configuration.getClientCredentialsProvider().get();
            final UserCredentials userCredentials = configuration.getUserCredentialsProvider().get();

            // prepare basic auth credentials
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(configuration.getAccessTokenUri().getHost(),
                            configuration.getAccessTokenUri().getPort()),
                    new UsernamePasswordCredentials(clientCredentials.getId(),
                            clientCredentials.getSecret()));

            // create a new client that targets our host with basic auth enabled
            final CloseableHttpClient client = HttpClients.custom()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .build();
            final HttpHost host = new HttpHost(configuration.getAccessTokenUri().getHost(),
                    configuration.getAccessTokenUri().getPort(), configuration.getAccessTokenUri().getScheme());
            final HttpPost request = new HttpPost(configuration.getAccessTokenUri());

            // prepare the request body

            final List<NameValuePair> values = new ArrayList<NameValuePair>() {{
                add(new BasicNameValuePair("grant_type", "password"));
                add(new BasicNameValuePair("username", userCredentials.getUsername()));
                add(new BasicNameValuePair("password", userCredentials.getPassword()));
                add(new BasicNameValuePair("scope", joinScopes(tokenConfig.getScopes())));
            }};
            request.setEntity(new UrlEncodedFormEntity(values));

            // enable basic auth for the request
            final AuthCache authCache = new BasicAuthCache();
            final BasicScheme basicAuth = new BasicScheme();
            authCache.put(host, basicAuth);

            final HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            // execute!
            final CloseableHttpResponse response = client.execute(host, request, localContext);
            try {
                // success status code?
                final int status = response.getStatusLine().getStatusCode();
                if (status < 200 || status >= 300) {
                    throw AccessTokenEndpointException.from(response);
                }

                // get json response
                final HttpEntity entity = response.getEntity();
                final AccessTokenResponse accessTokenResponse = OBJECT_MAPPER.readValue(EntityUtils.toByteArray(entity),
                        AccessTokenResponse.class);

                // create new access token object
                final Date validUntil = new Date(System.currentTimeMillis()
                        + (accessTokenResponse.expiresInSeconds * 1000));

                return new AccessToken(accessTokenResponse.getAccessToken(), accessTokenResponse.getTokenType(),
                        accessTokenResponse.getExpiresInSeconds(), validUntil);
            } finally {
                response.close();
            }
        } catch (Throwable t) {
            throw new AccessTokenEndpointException(t.getMessage(), t);
        }
    }

    @Override
    public String get(Object tokenId) throws AccessTokenUnavailableException {
        return getAccessToken(tokenId).getToken();
    }

    @Override
    public AccessToken getAccessToken(Object tokenId) throws AccessTokenUnavailableException {
        final AccessToken token = accessTokens.get(tokenId);
        if (token == null) {
            throw new AccessTokenUnavailableException("no token available");
        }
        if (token.isExpired()) {
            throw new AccessTokenUnavailableException("token expired");
        }
        return token;
    }

    @Override
    public void invalidate(final Object tokenId) {
        accessTokens.remove(tokenId);
    }

    @Override
    public void stop() {
        scheduler.shutdown();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class AccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("expires_in")
        private long expiresInSeconds;

        public String getAccessToken() {
            return accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public long getExpiresInSeconds() {
            return expiresInSeconds;
        }
    }
}
