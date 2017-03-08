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

import static java.util.stream.Stream.of;
import static org.zalando.stups.tokens.FileSupplier.getCredentialsDir;

import java.io.File;
import java.io.FilenameFilter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FilesystemReader<T> implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemReader.class);

    private final Function<File,T> mapper;
    private final Consumer<T> consumer;
    private final FilenameFilter filter;
    private final Predicate<T> predicate;

    FilesystemReader(Function<File, T> mapper, Consumer<T> consumer, Predicate<T> predicate, FilenameFilter filter) {
        this.mapper = mapper;
        this.consumer = consumer;
        this.filter = filter;
        this.predicate = predicate;
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
        final File[] tokenSecretFiles = getCredentialsDir().listFiles(filter);
        of(tokenSecretFiles)
                .map(mapper)
                .filter(predicate)
                .forEach(consumer);
    }
    //@formatter:on

}
