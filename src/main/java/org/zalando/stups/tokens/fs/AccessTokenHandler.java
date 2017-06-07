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

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.zalando.stups.tokens.EndsWithFilenameFilter.forSuffix;
import static org.zalando.stups.tokens.fs.FileUtils.readContent;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.stups.tokens.AccessToken;

class AccessTokenHandler implements Consumer<AccessTokenDto>, Function<File, AccessTokenDto>, Predicate<AccessTokenDto>,
        Supplier<FilenameFilter> {

    private static final String TOKEN_TYPE = "-token-type";

    private static final String TOKEN_SECRET = "-token-secret";

    private final Logger LOG = LoggerFactory.getLogger(AccessTokenHandler.class);

    private final Map<Object, AccessToken> target;
    private final TokenContentExtractor tokenContentExtractor;

    AccessTokenHandler(Map<Object, AccessToken> target, TokenContentExtractor tokenContentExtractor) {
        this.target = requireNonNull(target, "'target' should never be null");
        this.tokenContentExtractor = requireNonNull(tokenContentExtractor, "'tokenContentExtractor' should never be null");
    }

    @Override
    public void accept(AccessTokenDto t) {
        target.put(t.getName(), t);
    }

    @Override
    public AccessTokenDto apply(File tokenSecretFile) {
        final String name = tokenSecretFile.getName().replace(TOKEN_SECRET, "");

        try {
            String secret = readContent(tokenSecretFile.getAbsolutePath());
            String type = readContent(tokenSecretFile.toPath().resolveSibling(name + TOKEN_TYPE).toString());
            return new AccessTokenDto(tokenContentExtractor.extract(secret, type), name);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean test(AccessTokenDto t) {
        return nonNull(t) && "Bearer".equals(t.getType());
    }

    @Override
    public FilenameFilter get() {
        return forSuffix(TOKEN_SECRET);
    }

    public FilesystemReader<AccessTokenDto> getFilesystemReader() {
        return new FilesystemReader<AccessTokenDto>(this, this, this, get());
    }
}
