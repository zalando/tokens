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

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.mockito.internal.util.io.IOUtil;

/**
 * 
 * @author jbellmann
 *
 */
public class AccessTokenBuilderTest {

	private static final String CREDENTIALS_DIR = "CREDENTIALS_DIR";
	private static final String HTTP_EXAMPLE_ORG = "http://example.org";
	private URI uri = URI.create(HTTP_EXAMPLE_ORG);
	private ClientCredentialsProvider ccp = Mockito.mock(ClientCredentialsProvider.class);
	private UserCredentialsProvider ucp = Mockito.mock(UserCredentialsProvider.class);
	private HttpProviderFactory hpf = Mockito.mock(HttpProviderFactory.class);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void createCredentials() throws IOException {
		File tempDir = tempFolder.newFolder();
		File clientJson = new File(tempDir, "client.json");
		IOUtil.writeText("{\"client_id\":\"abcdefg \",\"client_secret\":\"geheim\"}", clientJson);
		File userJson = new File(tempDir, "user.json");
		IOUtil.writeText("{\"application_username\":\"klaus \",\"application_password\":\"geheim\"}", userJson);
		System.setProperty(CREDENTIALS_DIR, tempDir.getAbsolutePath());
	}

	@After
	public void resetSystemProperty() {
		System.getProperties().remove(CREDENTIALS_DIR);
	}

	@Test(expected = IllegalArgumentException.class)
	public void createByBuilderWithNull() {
		Tokens.createAccessTokensWithUri(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void clientCredentialsProviderShouldNotBeNull() {
		Tokens.createAccessTokensWithUri(uri).usingClientCredentialsProvider(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void userCredentialProviderShouldNotBeNull() {
		Tokens.createAccessTokensWithUri(uri).usingClientCredentialsProvider(ccp).usingUserCredentialsProvider(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void httpProxyFactoryShouldNotBeNull() {
		Tokens.createAccessTokensWithUri(uri).usingClientCredentialsProvider(ccp).usingUserCredentialsProvider(ucp)
				.usingHttpProviderFactory(null);
	}

	// GH-28
	@Test
	public void shouldItBePossibleToGetTokensWithoutScopesDefined() {
		// this is possible at the moment
		AccessTokens accessTokens = Tokens.createAccessTokensWithUri(uri).usingClientCredentialsProvider(ccp)
				.usingUserCredentialsProvider(ucp).usingHttpProviderFactory(hpf).manageToken("TOKEN_2").done()
				.start();

		Assertions.assertThat(accessTokens).isNotNull();
	}

	@Test
	public void buildAccessTokensWithDefault() throws IOException {

		AccessTokens accessTokens = Tokens.createAccessTokensWithUri(uri).manageToken("TOKEN_1").done().start();

		Assertions.assertThat(accessTokens).isNotNull();
	}

	@Test
	public void httpConfigShouldBeNotNull() {
		// this is possible at the moment
		HttpConfig httpConfig = Tokens.createAccessTokensWithUri(uri).usingClientCredentialsProvider(ccp)
				.usingUserCredentialsProvider(ucp).usingHttpProviderFactory(hpf).manageToken(new Object()).done()
				.getHttpConfig();

		Assertions.assertThat(httpConfig).isNotNull();

	}

    @Test(expected = IllegalArgumentException.class)
    public void accessTokenConfigurationWithoutScopesShouldFail() {
        Tokens.createAccessTokensWithUri(uri).start();
    }

    @Test(expected = IllegalArgumentException.class)
    public void providedExecutorServiceShouldNotBeNull() {
        Tokens.createAccessTokensWithUri(uri).existingExecutorService(null);
    }

    @Test
    public void defaultExecutorServiceShouldNotBeNull() {
        AccessTokensBuilder builder = Tokens.createAccessTokensWithUri(uri);
        ScheduledExecutorService executor = builder.getExecutorService();
        Assertions.assertThat(executor).isNotNull();
        executor.shutdownNow();
    }

    @Test
    public void noEnvironmentSet() {
        try {
            Tokens.createAccessTokens();
            Assertions.fail("Not expected to reach this point");
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("environment variable OAUTH2_ACCESS_TOKEN_URL not set");
        } finally {
            System.getProperties().remove("OAUTH2_ACCESS_TOKEN_URL");
        }
    }

    @Test
    public void notAnUri_OAUTH2_ACCESS_TOKEN_URL() {
        try {
			withEnvironmentVariable("OAUTH2_ACCESS_TOKEN_URL", "::::")
					.execute(Tokens::createAccessTokens);
            Assertions.fail("Not expected to reach this point");
        } catch (Exception e) {
            assertThat(e.getMessage())
                    .contains("environment variable OAUTH2_ACCESS_TOKEN_URL cannot be converted to an URI");
        }
    }

    @Test
    public void usinEnvCreatesBuilder() throws Exception {
        AccessTokensBuilder builder = withEnvironmentVariable("OAUTH2_ACCESS_TOKEN_URL", "https://somwhere.test/tokens")
						.execute(Tokens::createAccessTokens);
        Assertions.assertThat(builder).isNotNull();
    }
}
