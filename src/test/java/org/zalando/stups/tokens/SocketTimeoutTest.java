package org.zalando.stups.tokens;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Stopwatch;

public class SocketTimeoutTest {

    private HttpConfig httpConfig;
    private CloseableHttpClient client;
    private RequestConfig requestConfig;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().httpsPort(8443));

    /**
     * @see CloseableHttpProvider#createToken(AccessTokenConfiguration)
     */
    @Before
    public void setUp() {
        httpConfig = new HttpConfig();

        client = HttpClientFactory.createClient();
        //client = HttpClients.custom().setUserAgent(new UserAgent().get()).useSystemProperties().build();

        requestConfig = RequestConfig.custom().setSocketTimeout(httpConfig.getSocketTimeout())
                .setConnectTimeout(httpConfig.getConnectTimeout())
                .setConnectionRequestTimeout(httpConfig.getConnectionRequestTimeout())
                .setStaleConnectionCheckEnabled(httpConfig.isStaleConnectionCheckEnabled()).build();
    }

    @Test
    public void testHttpGet() {
        // configure a delay of 5000 ms
        stubFor(get(urlEqualTo("/socket")).willReturn(aResponse().withStatus(200).withFixedDelay(5000)));

        HttpGet getRequest = new HttpGet("https://localhost:8443/socket");
        // we set our custom-config with default-socket-timeout == 2000;
        Assertions.assertThat(requestConfig.getSocketTimeout()).isEqualTo(2000);
        getRequest.setConfig(requestConfig);

        Stopwatch sw = Stopwatch.createStarted();
        try {
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

        Assertions.assertThat(timeUsed).isLessThan(2200);
    }
}
