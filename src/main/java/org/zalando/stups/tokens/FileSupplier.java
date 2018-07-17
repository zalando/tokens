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

import org.apache.http.util.Args;

import java.io.File;
import java.util.Optional;

/**
 * Replacement to make it compatible with Java7.
 *
 * @author  jbellmann
 */
public class FileSupplier {

    private static final String DEFAULT_CREDENTIALS_DIR = "/meta/credentials";

    private static final String CREDENTIALS_DIR_PROP = "CREDENTIALS_DIR";

    private File file;
    private final String filename;

    FileSupplier(final File file) {
        this.file = Args.notNull(file, "file");
        this.filename = null;
    }

    FileSupplier(final String filename) {
        this.filename = Args.notBlank(filename, "filename");
    }

    public synchronized File get() {
        if (file != null) {
            return file;
        } else {
            file = new File(getCredentialsDir(), filename);
            return file;
        }
    }

    public static File getCredentialsDir() {
        return credentialsDir()
                .map(File::new)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("environment variable %s not set and default '%s' does not exist or is not a directory.", CREDENTIALS_DIR_PROP, DEFAULT_CREDENTIALS_DIR)
                ));
    }

    public static Optional<String> credentialsDir() {
        Optional<String> optionalDir = Optional.ofNullable(System.getenv(CREDENTIALS_DIR_PROP));
        optionalDir = optionalDir.isPresent() ? optionalDir : Optional.ofNullable(System.getProperty(CREDENTIALS_DIR_PROP));
        return optionalDir.isPresent() ? optionalDir : defaultCredentialsDir();
    }

    protected static Optional<String> defaultCredentialsDir(){
        if (new File(DEFAULT_CREDENTIALS_DIR).exists() && new File(DEFAULT_CREDENTIALS_DIR).isDirectory()) {
            return Optional.of(DEFAULT_CREDENTIALS_DIR);
        } else {
            return Optional.empty();
        }
    }
}
