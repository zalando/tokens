package org.zalando.stups.tokens;

import java.util.Set;

interface TokensConfiguration {

    // TODO rename
    Set<AccessTokenConfiguration> getAccessTokenConfigurations();

    boolean isValidateTokensOnStartup();

    MetricsListener getMetricsListener();

}
