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

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.zalando.stups.tokens.FileUtils.readContent;

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
import org.zalando.stups.tokens.ClientCredentials;

class ClientCredentialsHandler implements Consumer<SimpleClientCredentials>, Function<File, SimpleClientCredentials>,
        Predicate<SimpleClientCredentials>, Supplier<FilenameFilter> {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCredentialsHandler.class);

    private static final String CLIENT_SECRET = "-client-secret";

    private static final String TEMPLATE_SUFFIX = "-client-id";

    private final Map<String, ClientCredentials> target;

    ClientCredentialsHandler(Map<String, ClientCredentials> target) {
        this.target = requireNonNull(target, "'target' should not be null");
    }

    @Override
    public FilenameFilter get() {
        return EndsWithFilenameFilter.forSuffix(TEMPLATE_SUFFIX);
    }

    @Override
    public boolean test(SimpleClientCredentials t) {
        return nonNull(t);
    }

    @Override
    public SimpleClientCredentials apply(File t) {
        final String name = t.getName().replace(TEMPLATE_SUFFIX, "");

        try {
            String id = readContent(t.getAbsolutePath());
            String secret = readContent(t.toPath().resolveSibling(name + CLIENT_SECRET).toString());
            return new SimpleClientCredentials(name, id, secret);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void accept(SimpleClientCredentials t) {
        this.target.put(t.getName(), t);
    }

    public FilesystemReader<?> getFilesystemReader() {
        return new FilesystemReader<>(this, this, this, get());
    }

}
