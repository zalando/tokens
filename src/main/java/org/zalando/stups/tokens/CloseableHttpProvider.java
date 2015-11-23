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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CloseableHttpProvider extends AbstractHttpProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CloseableHttpClient client;
    private final HttpClientContext localContext;
    private UserCredentials userCredentials;
    private URI accessTokenUri;
    private final HttpHost host;

    public CloseableHttpProvider(ClientCredentials clientCredentials, UserCredentials userCredentials, URI accessTokenUri) {
        this.userCredentials = userCredentials;
        this.accessTokenUri = accessTokenUri;

        // prepare basic auth credentials
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(accessTokenUri.getHost(),
                        accessTokenUri.getPort()),
                new UsernamePasswordCredentials(clientCredentials.getId(), clientCredentials.getSecret()));

        client = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider)
                .build();

        host = new HttpHost(accessTokenUri.getHost(),
                accessTokenUri.getPort(), accessTokenUri.getScheme());

        // enable basic auth for the request
        final AuthCache authCache = new BasicAuthCache();
        final BasicScheme basicAuth = new BasicScheme();
        authCache.put(host, basicAuth);

        localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);
    }

    public AccessToken createToken(final AccessTokenConfiguration tokenConfig) throws UnsupportedEncodingException {
        final List<NameValuePair> values = buildParameterList(tokenConfig);

        final HttpPost request = new HttpPost(accessTokenUri);
        request.setEntity(new UrlEncodedFormEntity(values));

        try (final CloseableHttpResponse response = client.execute(host, request, localContext)) {

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
                    + (accessTokenResponse.getExpiresInSeconds() * 1000));

            return new AccessToken(accessTokenResponse.getAccessToken(), accessTokenResponse.getTokenType(),
                    accessTokenResponse.getExpiresInSeconds(), validUntil);
        } catch (Throwable t) {
            throw new AccessTokenEndpointException(t.getMessage(), t);
        }
    }

    private List<NameValuePair> buildParameterList(final AccessTokenConfiguration tokenConfig) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", "password"));
        nameValuePairs.add(new BasicNameValuePair("username", userCredentials.getUsername()));
        nameValuePairs.add(new BasicNameValuePair("password", userCredentials.getPassword()));
        nameValuePairs.add(new BasicNameValuePair("scope", joinScopes(tokenConfig.getScopes())));
        return nameValuePairs;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
