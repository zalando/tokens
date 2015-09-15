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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author  jbellmann
 */
public class FileSupplierTest {

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

    @Test(expected = IllegalStateException.class)
    public void testNoCredentialsDirSet() {
        FileSupplier supplier = new FileSupplier("notExistent");
        Assert.assertNotNull(supplier);
        supplier.get();
    }

}
