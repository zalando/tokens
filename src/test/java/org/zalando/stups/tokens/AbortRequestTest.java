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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Stopwatch;

@Ignore
public class AbortRequestTest {

    private HttpConfig httpConfig;
    private CloseableHttpClient client;
    private RequestConfig requestConfig;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8989);

    /**
     * @see CloseableHttpProvider#createToken(AccessTokenConfiguration)
     */
    @Before
    public void setUp() {
        httpConfig = new HttpConfig();

        client = HttpClients.custom().setUserAgent(new UserAgent().get()).useSystemProperties().build();

        requestConfig = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(httpConfig.getConnectTimeout())
                .setConnectionRequestTimeout(httpConfig.getConnectionRequestTimeout())
                .setStaleConnectionCheckEnabled(httpConfig.isStaleConnectionCheckEnabled()).build();
    }

    @Test
    public void testHttpGet() throws InterruptedException {
        // configure a delay of 15000 ms
        stubFor(get(urlEqualTo("/socket")).willReturn(aResponse().withStatus(200).withFixedDelay(15000)));
        // we set our custom-config with default-socket-timeout == 2000;
        Assertions.assertThat(requestConfig.getSocketTimeout()).isEqualTo(15000);

        ExecutorService executorService = Executors.newFixedThreadPool(8);

        for (int i = 0; i < 5; i++) {
            executorService.submit(new RequestRunner(client, requestConfig));
            TimeUnit.SECONDS.sleep(2);
        }

        TimeUnit.MINUTES.sleep(1);
    }

    static class RequestRunner implements Callable<Boolean> {

        private final CloseableHttpClient client;
        private final RequestConfig requestConfig;

        public RequestRunner(CloseableHttpClient client, RequestConfig requestConfig) {
            this.client = client;
            this.requestConfig = requestConfig;
        }

        @Override
        public Boolean call() throws Exception {

            final HttpGet getRequest = new HttpGet("http://localhost:8989/socket");
            getRequest.setConfig(requestConfig);

            Stopwatch sw = Stopwatch.createStarted();
            try {
                new Timer(true).schedule(new TimerTask() {

                    @Override
                    public void run() {
                        if (getRequest != null) {
                            System.out.println("TRY TO ABORT");
                            getRequest.abort();
                        }
                    }
                }, 8000);
                client.execute(getRequest);
                Assertions.fail("Expect an SocketTimeoutException, here");
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                sw.stop();
            }

            long timeUsed = sw.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("TIMEUSED: " + timeUsed + " ms");
            return true;
        }
    }
}
