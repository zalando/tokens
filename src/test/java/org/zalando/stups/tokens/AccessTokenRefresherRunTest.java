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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.internal.util.io.IOUtil;

public class AccessTokenRefresherRunTest {

	private static final String CREDENTIALS_DIR = "CREDENTIALS_DIR";
	private static final String HTTP_EXAMPLE_ORG = "http://example.org";
	private URI uri = URI.create(HTTP_EXAMPLE_ORG);

	private AccessTokens accessTokens;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@After
	public void resetSystemProperty() {
		System.getProperties().remove(CREDENTIALS_DIR);
		System.clearProperty("OAUTH2_ACCESS_TOKENS");
	}

	@After
	public void shutdownAccessTokens() {
		if (accessTokens != null) {
			accessTokens.stop();
		}
	}

	@Test
	public void runAccessTokenRefresher() throws IOException, InterruptedException {
		File tempDir = tempFolder.newFolder();
		File clientJson = new File(tempDir, "client.json");
		IOUtil.writeText("{\"client_id\":\"abcdefg \",\"client_secret\":\"geheim\"}", clientJson);
		File userJson = new File(tempDir, "user.json");
		IOUtil.writeText("{\"application_username\":\"klaus \",\"application_password\":\"geheim\"}", userJson);
		System.setProperty(CREDENTIALS_DIR, tempDir.getAbsolutePath());

		ClientCredentialsProvider ccp = Mockito.mock(ClientCredentialsProvider.class);
		UserCredentialsProvider ucp = Mockito.mock(UserCredentialsProvider.class);
		HttpProviderFactory hpf = Mockito.mock(HttpProviderFactory.class);

		AccessTokensBuilder accessTokenBuilder = Tokens.createAccessTokensWithUri(uri).usingClientCredentialsProvider(ccp)
				.usingUserCredentialsProvider(ucp).usingHttpProviderFactory(hpf).manageToken("TR_TEST").done();

		HttpProvider httpProvider = Mockito.mock(HttpProvider.class);

		Mockito.when(hpf.create(Mockito.any(ClientCredentials.class), Mockito.any(UserCredentials.class),
				Mockito.any(URI.class), Mockito.any(HttpConfig.class))).thenReturn(httpProvider);

		Mockito.when(httpProvider.createToken(Mockito.any(AccessTokenConfiguration.class)))
				.thenReturn(new AccessToken("123456789", "BEARER", 2, new Date(System.currentTimeMillis() + 15000)));
		accessTokens = accessTokenBuilder.start();
		Assertions.assertThat(accessTokens).isNotNull();

		TimeUnit.SECONDS.sleep(30);

		Mockito.verify(httpProvider, Mockito.atLeastOnce()).createToken(Mockito.any(AccessTokenConfiguration.class));

	}

	/**
	 * Verifies that if a fixed token is set and the default configuration used, no attempt to refresh it using an
	 * {@link HttpProvider} is done.
	 */
	@Test
	public void runAccessTokenRefresherWithFixedToken() throws InterruptedException {
		System.setProperty("OAUTH2_ACCESS_TOKENS", "pierone=987654321");

		accessTokens = Tokens.createAccessTokensWithUri(uri).manageToken("pierone").done().start();
	}
}
