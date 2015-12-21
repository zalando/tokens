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

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 
 * @author jbellmann
 *
 */
public class AccessTokenBuilderTest {

	private static final String HTTP_EXAMPLE_ORG = "http://example.org";
	URI uri = URI.create(HTTP_EXAMPLE_ORG);
	private ClientCredentialsProvider ccp = Mockito.mock(ClientCredentialsProvider.class);
	private UserCredentialsProvider ucp = Mockito.mock(UserCredentialsProvider.class);
	private HttpProviderFactory hpf = Mockito.mock(HttpProviderFactory.class);

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

	@Ignore
	@Test
	public void shouldItBePossibleToGetTokensWithoutScopesDefined() {
		// this is possible at the moment
		Tokens.createAccessTokensWithUri(URI.create(HTTP_EXAMPLE_ORG)).usingClientCredentialsProvider(ccp)
				.usingUserCredentialsProvider(ucp).usingHttpProviderFactory(hpf).manageToken(new Object()).done()
				.start();
	}

}
