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

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractJsonFileBackedCredentialsProvider {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FileSupplier fileSupplier;

    public AbstractJsonFileBackedCredentialsProvider(final String filename) {
        this.fileSupplier = new FileSupplier(filename);
    }

    public AbstractJsonFileBackedCredentialsProvider(final File file) {
        this.fileSupplier = new FileSupplier(file);
    }

    protected File getFile() {
        return fileSupplier.get();
    }

    protected <T> T read(final Class<T> cls) {
        try {
            return OBJECT_MAPPER.readValue(getFile(), cls);
        } catch (final Throwable e) {
            throw new CredentialsUnavailableException(e.getMessage(), e);
        }
    }
}
