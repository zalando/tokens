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

/**
 * Replacement to make it compatible with Java7.
 *
 * @author  jbellmann
 */
class FileSupplier {

    private File file;
    private final String filename;

    public FileSupplier(final File file) {
        this.file = file;
        this.filename = null;
    }

    public FileSupplier(final String filename) {
        this.filename = filename;
    }

    public synchronized File get() {
        if (file != null) {
            return file;
        } else {
            file = new File(getCredentialsDir(), filename);
            return file;
        }
    }

    private static File getCredentialsDir() {
        final String dir = System.getenv("CREDENTIALS_DIR");
        if (dir == null) {
            throw new IllegalStateException("environment variable CREDENTIALS_DIR not set");
        }

        return new File(dir);
    }

}
