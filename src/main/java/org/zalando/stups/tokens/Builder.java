package org.zalando.stups.tokens;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public interface Builder {

    default Builder manage(final String tokenId, final String... scopes) {
        return manage(tokenId, unmodifiableSet(new LinkedHashSet<>(Arrays.asList(scopes))));
    }

    Builder manage(String tokenId, Set<String> scopes);

    default Builder validateTokensOnStartup() {
        return validateTokensOnStartup(true);
    }

    Builder validateTokensOnStartup(boolean validateTokensOnStartup);

    Builder withMetricsListener(MetricsListener metricsListener);

    AccessTokens build();

}
