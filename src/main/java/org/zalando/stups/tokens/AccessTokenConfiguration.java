package org.zalando.stups.tokens;

import lombok.Value;

import java.util.Set;

/**
 * Configuration for one single access token that is managed by the {@link AccessTokens} service.
 * It is used at configuration time and is retrieved via
 * {@link Builder#manage(String, String[])}. It is possible to configure the desired
 * <i>scopes</i> (see https://tools.ietf.org/html/rfc6749#section-3.3 for details) for this
 * access token. It follows a fluent interface design pattern.
 *
 */
@Value
class AccessTokenConfiguration {

	String tokenId;
	Set<String> scopes;

}
