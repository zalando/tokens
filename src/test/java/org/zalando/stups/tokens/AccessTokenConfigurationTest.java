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
		String scope = "somescope";
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		configuration.addScope(scope);
		Assertions.assertThat(configuration.getScopes()).containsExactly(scope);
	}

	@Test
	public void addStringScope() {
		String scope = "scope";
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		configuration.addScope(scope);
		Assertions.assertThat(configuration.getScopes()).containsExactly(scope);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingNullScope() {
		String scope = null;
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		configuration.addScope(scope);
		Assertions.fail("This code should not be reached");
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingEmptyScope() {
		String scope = "";
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		configuration.addScope(scope);
		Assertions.fail("This code should not be reached");
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingBlankScope() {
		String scope = " ";
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		configuration.addScope(scope);
		Assertions.fail("This code should not be reached");
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingScopes() {
		String scope = "scope";
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		List<String> scopes = new ArrayList<>();
		scopes.add(null);
		scopes.add(scope);
		configuration.addScopesTypeSafe(scopes);
		Assertions.fail("This code should not be reached");
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingNullScopesTypeSafe() {
		String scope = "scope";
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		List<String> scopes = new ArrayList<>();
		scopes.add(null);
		scopes.add(scope);
		configuration.addScopesTypeSafe(scopes);
		Assertions.fail("This code should not be reached");
	}

	@Test(expected = IllegalArgumentException.class)
	public void addingBlankScopesTypeSafe() {
		String scope = "scope";
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		List<String> scopes = new ArrayList<>();
		scopes.add(" ");
		scopes.add(scope);
		configuration.addScopesTypeSafe(scopes);
		Assertions.fail("This code should not be reached");
	}

	@Test
	public void addingValidScopes() {
		String scope = "anotherScope";
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		List<String> scopes = new ArrayList<>();
		scopes.add("scope");
		scopes.add(scope);
		configuration.addScopesTypeSafe(scopes);
		Assertions.assertThat(configuration.getScopes()).contains(scope);
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
		configuration.addScope("scope");
	}

	@Test(expected = IllegalStateException.class)
	public void addingScopesNotAllowedAfterDone() {
		AccessTokenConfiguration configuration = new AccessTokenConfiguration(new Object(), builder);
		AccessTokensBuilder builder = configuration.done();
		Assertions.assertThat(builder).isNotNull();
		configuration.addScopesTypeSafe(Collections.singleton(""));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullGrantType() {
		new AccessTokenConfiguration(new Object(), builder).withGrantType(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyGrantType() {
		new AccessTokenConfiguration(new Object(), builder).withGrantType("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWhitespaceGrantType() {
		new AccessTokenConfiguration(new Object(), builder).withGrantType("  ");
	}

	@Test
	public void testValidArgumentGrantType() {
		new AccessTokenConfiguration(new Object(), builder).withGrantType("a_ValidArgument_But_Invalid_Grant_Type");
	}

}
