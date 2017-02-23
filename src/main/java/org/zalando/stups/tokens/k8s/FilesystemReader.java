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
package org.zalando.stups.tokens.k8s;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.tokens.FileSupplier.getCredentialsDir;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.stups.tokens.AccessToken;
import org.zalando.stups.tokens.EndsWithFilenameFilter;

public class FilesystemReader implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemReader.class);

    private static final String TOKEN_SECRET_SUFFIX = "-token-secret";
    private static final String TOKEN_TYPE_SUFFIX = "-token-type";

    private final FilenameFilter endsWithSuffixFilenameFilter = EndsWithFilenameFilter.forSuffix(TOKEN_SECRET_SUFFIX);

    private final Map<Object, AccessToken> accessTokens;

    public FilesystemReader(Map<Object, AccessToken> accessTokens) {
        this.accessTokens = accessTokens;
    }

    @Override
    public void run() {
        try {
            readFromFilesystem();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    //@formatter:off
    protected void readFromFilesystem() {
        LOG.debug("read from filesystem ...");
        final File[] tokenSecretFiles = getCredentialsDir().listFiles(endsWithSuffixFilenameFilter);
        asList(tokenSecretFiles)
                .stream()
                .map(f -> buildAccessTokenDto(f))
                .filter(Objects::nonNull)
                .collect(toList())
                .forEach(it -> {
                    accessTokens.put(it.getName(),it);
                });
    }
    //@formatter:on

    protected AccessTokenDto buildAccessTokenDto(File tokenSecretFile) {
        final String name = tokenSecretFile.getName().replace(TOKEN_SECRET_SUFFIX, "");

        try {
            String secret = FileUtils.readContent(tokenSecretFile.getAbsolutePath());
            String type = FileUtils.readContent(tokenTypeFilePath(tokenSecretFile.getParentFile(), name));
            return new AccessTokenDto(secret, type, name);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    protected String tokenTypeFilePath(File parentDirectory, String name) {
        return new File(parentDirectory, name + TOKEN_TYPE_SUFFIX).getAbsolutePath();
    }

}
