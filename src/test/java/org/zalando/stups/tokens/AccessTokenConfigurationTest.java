package org.zalando.stups.tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Used by {@link AccessTokensBuilder}.
 * 
 * @author jbellmann
 * 
 * @see AccessTokensBuilder
 * @see AccessTokenConfiguration
 *
 */
public class AccessTokenConfigurationTest {

	private static final String DEFAULT_GRANT_TYPE_SHOULD_BE_PASSWORD = "password";
	private AccessTokensBuilder builder = Mockito.mock(AccessTokensBuilder.class);

	@Test(expected = IllegalArgumentException.class)
	public void tokenIdShouldNeverBeNull() {
		new AccessTokenConfiguration(null, builder);
	}

	@Test(expected = IllegalArgumentException.class)
	public void accessTokenBuilderShouldNeverBeNull() {
		new AccessTokenConfiguration(new Object(), null);
	}

	@Test
	public void defaultGrantTypeShouldBeDefaultGrantType() {
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		Assertions.assertThat(configuration.getGrantType()).isEqualTo(AccessTokenConfiguration.DEFAULT_GRANT_TYPE);
		Assertions.assertThat(configuration.getGrantType()).isNotEmpty();
	}

	@Test
	public void defaultGrantTypeShouldBePassword() {
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		Assertions.assertThat(configuration.getGrantType()).isEqualTo(AccessTokenConfiguration.DEFAULT_GRANT_TYPE);
		Assertions.assertThat(configuration.getGrantType()).isEqualTo(DEFAULT_GRANT_TYPE_SHOULD_BE_PASSWORD);
	}

	@Test
	public void addScope() {
		Object scope = new Object();
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		configuration.addScope(scope);
		Assertions.assertThat(configuration.getScopes()).containsExactly(scope);
	}
	
	@Test
	public void addingScopes() {
		Object scope = new Object();
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		List<Object> scopes = new ArrayList<>();
		scopes.add(null);
		scopes.add(scope);
		configuration.addScopes(scopes);
		Assertions.assertThat(configuration.getScopes()).containsExactly(scope);
	}

	@Test
	public void getScopeShouldBeSameAsProvidedInConstructor() {
		Object tokenId = new Object();
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(tokenId, builder);
		Assertions.assertThat(configuration.getTokenId()).isSameAs(tokenId);
	}

	@Test(expected = IllegalStateException.class)
	public void addingScopeNotAllowedAfterDone() {
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		AccessTokensBuilder builder = configuration.done();
		Assertions.assertThat(builder).isNotNull();
		configuration.addScope(new Object());
	}

	@Test(expected = IllegalStateException.class)
	public void addingScopesNotAllowedAfterDone() {
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		AccessTokensBuilder builder = configuration.done();
		Assertions.assertThat(builder).isNotNull();
		configuration.addScopes(Collections.singleton(new Object()));
	}

}
