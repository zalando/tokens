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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  jbellmann
 */
public class FileSupplierTest {

    private static final Logger LOG = LoggerFactory.getLogger(FileSupplier.class);

    @Test(expected = IllegalArgumentException.class)
    public void testFileConstructorParam() {
        File file = null;
        new FileSupplier(file);
    }

    @Test
    public void testNonNullFileConstructorParam() {
        FileSupplier supplier = new FileSupplier(new File("notExistent"));
        Assert.assertNotNull(supplier);
        supplier.get();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilenameParameterIsNull() {
        String filename = null;
        new FileSupplier(filename);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilenameParameterIsEmpty() {
        FileSupplier supplier = new FileSupplier("");
        Assert.assertNotNull(supplier);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFilenameParameterWithWhitespace() {
        FileSupplier supplier = new FileSupplier("  ");
        Assert.assertNotNull(supplier);
    }

    @Test
    public void testFilenameParameterIsNotNull() {
        System.setProperty("CREDENTIALS_DIR", "notexistign_path");

        FileSupplier supplier = new FileSupplier("notExistent");
        Assert.assertNotNull(supplier);
        supplier.get();
    }

    @Test
    public void testNoCredentialsDirSet() {
        if (System.getenv("CREDENTIALS_DIR") != null) {
            LOG.warn("ENV 'CREDENTIALS_DIR' was set so we skip");
            return;
        }
        try {
            FileSupplier supplier = new FileSupplier("notExistent");
            Assert.assertNotNull(supplier);
            supplier.get();
            Assertions.fail("expect an exception, when 'CREDENTIALS_DIR' not set in environment");
        } catch (IllegalStateException t) {
        }
    }

}
