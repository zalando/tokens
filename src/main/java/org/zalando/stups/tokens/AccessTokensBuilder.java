package org.zalando.stups.tokens;

import lombok.Value;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

/**
 * Use the <i>AccessTokensBuilder</i> obtained via
 * {@link Tokens#create()} to build your configuration for
 * obtaining an {@link AccessToken} for different scopes / services via the
 * {@link AccessTokens#getAccessToken(Object)} or
 * {@link AccessTokens#get(Object)} methods on the instance returned after
 * invoking {@link AccessTokensBuilder#build()}.
 */
@Value
final class AccessTokensBuilder implements Builder, TokensConfiguration {

    private final Map<String, Set<String>> tokenConfigurations;
    private final boolean validateTokensOnStartup;
    private final MetricsListener metricsListener;

    AccessTokensBuilder() {
        this(Collections.emptyMap(), false, new DebugLogMetricsListener());
    }

    private AccessTokensBuilder(final Map<String, Set<String>> tokenConfigurations,
            final boolean validateTokensOnStartup, final MetricsListener metricsListener) {
        this.tokenConfigurations = tokenConfigurations;
        this.metricsListener = metricsListener;
        this.validateTokensOnStartup = validateTokensOnStartup;
    }

    @Override
    public Set<AccessTokenConfiguration> getAccessTokenConfigurations() {
        return tokenConfigurations.entrySet().stream()
                .map(e -> new AccessTokenConfiguration(e.getKey(), e.getValue()))
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    public AccessTokens build() {
        return new FilesystemSecretRefresher(this);
    }

    @Override
    public Builder manage(final String tokenId, final Set<String> scopes) {
        return new AccessTokensBuilder();
    }

    @Override
    public Builder validateTokensOnStartup(final boolean validateTokensOnStartup) {
        return new AccessTokensBuilder(tokenConfigurations, validateTokensOnStartup, metricsListener);
    }

    @Override
    public Builder withMetricsListener(final MetricsListener metricsListener) {
        return new AccessTokensBuilder(tokenConfigurations, validateTokensOnStartup, metricsListener);
    }

}
