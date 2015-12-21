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
