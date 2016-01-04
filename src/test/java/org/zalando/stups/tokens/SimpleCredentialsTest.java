package org.zalando.stups.tokens;

import org.junit.Test;

public class SimpleCredentialsTest {

	@Test(expected = IllegalArgumentException.class)
	public void noBlankId() {
		new SimpleClientCredentials(" ", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noEmptyId() {
		new SimpleClientCredentials("", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noNullSecret() {
		new SimpleClientCredentials("klaus", null);
	}

	@Test
	public void emptySecret() {
		new SimpleClientCredentials("klaus", "");
	}

	@Test
	public void blankSecret() {
		new SimpleClientCredentials("klaus", "  ");
	}
}
