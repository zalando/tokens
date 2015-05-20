package org.zalando.stups.tokens;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class AccessTokenRefresher implements AccessTokens, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AccessTokenRefresher.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AccessTokensBuilder configuration;
    private final ScheduledExecutorService scheduler;

    private ConcurrentHashMap<Object, AccessToken> accessTokens = new ConcurrentHashMap<Object, AccessToken>();

    public AccessTokenRefresher(final AccessTokensBuilder configuration) {
        this.configuration = configuration;

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(this, 1, TimeUnit.SECONDS);
    }

    private int percentLeft(final AccessToken token) {
        final long now = System.currentTimeMillis();
        final long validUntil = token.getValidUntil().getTime();
        final long hundredPercentSeconds = token.getInitialValidSeconds();
        final long secondsLeft = validUntil - now;
        return (int) (secondsLeft / hundredPercentSeconds * 100);
    }

    private boolean shouldRefresh(final AccessToken token) {
        return percentLeft(token) <= configuration.getRefreshPercentLeft();
    }

    private boolean shouldWarn(final AccessToken token) {
        return percentLeft(token) <= configuration.getWarnPercentLeft();
    }

    @Override
    public void run() {
        try {
            for (final AccessTokenConfiguration tokenConfig : configuration.getAccessTokenConfigurations()) {
                final AccessToken oldToken = accessTokens.get(tokenConfig.getTokenId());
                // TODO optionally check with tokeninfo endpoint regularly (every x% of time)
                if (oldToken == null || shouldRefresh(oldToken)) {
                    try {
                        LOG.trace("Refreshing access token {}...", tokenConfig.getTokenId());
                        final AccessToken newToken = createToken(tokenConfig);
                        accessTokens.put(tokenConfig.getTokenId(), newToken);
                        LOG.debug("Refreshed access token {}.", tokenConfig.getTokenId());
                    } catch (final Throwable t) {
                        if (oldToken == null || shouldWarn(oldToken)) {
                            LOG.warn("Cannot refresh access token {} because {}.", tokenConfig.getTokenId(), t);
                        }
                    }
                }
            }
        } catch (final Throwable t) {
            LOG.error("Unexpected problem during token refresh run!", t);
        }
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
            final AccessTokenRequest accessTokenRequest = new AccessTokenRequest(userCredentials.getUsername(),
                    userCredentials.getPassword(), tokenConfig.getScopes());
            final StringEntity accessTokenRequestBody = new StringEntity(OBJECT_MAPPER
                    .writeValueAsString(accessTokenRequest));
            accessTokenRequestBody.setContentType("application/json");
            request.setEntity(accessTokenRequestBody);

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
                    throw new AccessTokenEndpointException(response.getStatusLine().toString());
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
        if (token == null || token.isExpired()) {
            throw new AccessTokenUnavailableException();
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

    private static final class AccessTokenRequest {
        @JsonProperty("grant_type")
        private final String grantType = "password";

        @JsonProperty("username")
        private final String username;

        @JsonProperty("password")
        private final String password;

        @JsonProperty("scope")
        private final String scope;

        private static String join(final Collection<Object> scopes) {
            final Iterator<Object> iter = scopes.iterator();

            final StringBuilder scope = new StringBuilder(iter.next().toString());
            while (iter.hasNext()) {
                scope.append(' ');
                scope.append(iter.next().toString());
            }

            return scope.toString();
        }

        public AccessTokenRequest(final String username, final String password, Collection<Object> scopes) {
            this.username = username;
            this.password = password;
            this.scope = join(scopes);
        }

        public AccessTokenRequest(final String username, final String password, final String scope) {
            this.username = username;
            this.password = password;
            this.scope = scope;
        }
    }

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
