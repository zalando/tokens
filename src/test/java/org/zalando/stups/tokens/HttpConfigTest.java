package org.zalando.stups.tokens;

import java.net.URI;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * 
 * @author jbellmann
 *
 */
public class HttpConfigTest {

	private static final String HTTP_EXAMPLE_ORG = "http://example.org";
	private URI uri = URI.create(HTTP_EXAMPLE_ORG);

	private final int testValue = 5000;

	@Test
	public void expectedDefaults() {
		HttpConfig config = new HttpConfig();

		Assertions.assertThat(config.getConnectionRequestTimeout()).isEqualTo(500);
		Assertions.assertThat(config.getConnectTimeout()).isEqualTo(1000);
		Assertions.assertThat(config.getSocketTimeout()).isEqualTo(2000);
		Assertions.assertThat(config.isStaleConnectionCheckEnabled()).isTrue();
	}

	@Test
	public void builderUsage() {
		HttpConfig config = Tokens.createAccessTokensWithUri(uri).socketTimeout(testValue)
				.connectionRequestTimeout(testValue).connectTimeout(testValue).schedulingPeriod(testValue).staleConnectionCheckEnabled(false).getHttpConfig();

		Assertions.assertThat(config.getConnectionRequestTimeout()).isEqualTo(testValue);
		Assertions.assertThat(config.getConnectTimeout()).isEqualTo(testValue);
		Assertions.assertThat(config.getSocketTimeout()).isEqualTo(testValue);
		Assertions.assertThat(config.isStaleConnectionCheckEnabled()).isFalse();
	}

}
