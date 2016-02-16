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

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author jbellmann
 *
 */
class CloseableTokenVerifier implements TokenVerifier {

    private static final String METRICS_KEY = "tokens.verifier";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RequestConfig requestConfig;
    private final CloseableHttpClient client;
    private URI tokenInfoUri;
    private final HttpHost host;
    private final MetricsListener metricsListener;

    public CloseableTokenVerifier(URI tokenInfoUri, HttpConfig httpConfig, MetricsListener metricsListener) {
        this.tokenInfoUri = tokenInfoUri;
        this.metricsListener = metricsListener;

        requestConfig = RequestConfig.custom().setSocketTimeout(httpConfig.getSocketTimeout())
                .setConnectTimeout(httpConfig.getConnectTimeout())
                .setConnectionRequestTimeout(httpConfig.getConnectionRequestTimeout())
                .setStaleConnectionCheckEnabled(httpConfig.isStaleConnectionCheckEnabled()).build();

        client = HttpClients.custom().setUserAgent(new UserAgent().get()).useSystemProperties().build();

        host = new HttpHost(tokenInfoUri.getHost(), tokenInfoUri.getPort(), tokenInfoUri.getScheme());
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public boolean isTokenValid(String token) {

        final HttpGet request = new HttpGet(tokenInfoUri);
        request.setHeader(ACCEPT, APPLICATION_JSON.getMimeType());
        request.setHeader(AUTHORIZATION, "Bearer " + token);
        request.setConfig(requestConfig);

        long start = System.currentTimeMillis();
        try (final CloseableHttpResponse response = client.execute(host, request)) {

            // success status code?
            final int status = response.getStatusLine().getStatusCode();
            if (status < 400) {
                // seems to be ok
                return true;
            } else if (status >= 400 && status < 500) {
                // get json response
                final HttpEntity entity = response.getEntity();
                final ProblemResponse problemResponse = objectMapper.readValue(EntityUtils.toByteArray(entity),
                        ProblemResponse.class);
                // do we want to do something with problem?
                // seems to be invalid
                return false;
            }

        } catch (Throwable t) {
            // how to handle this? For now, do not delete the token
            return true;
        } finally {
            long time = System.currentTimeMillis() - start;
            metricsListener.submitToTimer(METRICS_KEY, time);
        }
        return true;
    }

}
