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
package org.zalando.stups.tokens.fs;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.stups.tokens.AbstractAccessTokenRefresher;
import org.zalando.stups.tokens.TokenRefresherConfiguration;

public class FilesystemSecretRefresher extends AbstractAccessTokenRefresher {

    private static final Logger LOG = LoggerFactory.getLogger(FilesystemSecretRefresher.class);

    public FilesystemSecretRefresher(TokenRefresherConfiguration configuration) {
        super(configuration);
    }


    @Override
    public void start() {
        initializeFixedTokensFromEnvironment();
        LOG.info("Starting to refresh tokens regularly from filesystem ...");

        FilesystemReader reader = new FilesystemReader(accessTokens);

        if (configuration.getKubernetesConfiguration().isValidateTokensOnStartup()) {
            reader.readFromFilesystem();
            List<?> missing = findMissingTokens();
            if (missing.size() > 0) {
                throw new TokensMissingException(missing);
            }
        }

        scheduler.scheduleAtFixedRate(reader, 1, configuration.getSchedulingPeriod(),
                configuration.getSchedulingTimeUnit());
    }
    
    //@formatter:off
    protected List<?> findMissingTokens() {
        return configuration.getAccessTokenConfigurations()
                            .stream()
                            .map(atc -> atc.getTokenId())
                            .filter(tokenId -> !accessTokens.containsKey(tokenId))
                            .collect(toList());
    }
    //@formatter:on
}
