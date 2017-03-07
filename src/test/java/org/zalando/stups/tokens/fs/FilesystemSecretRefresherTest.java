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

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.tokens.AccessTokens;
import org.zalando.stups.tokens.Secrets;
import org.zalando.stups.tokens.Tokens;

public class FilesystemSecretRefresherTest {

    @Before
    public void setup() {
        System.getProperties().put("CREDENTIALS_DIR", "fs");
    }

    @After
    public void tearDown() {
        System.getProperties().remove("CREDENTIALS_DIR");
    }

    //@formatter:off
    @Test
    public void test() throws InterruptedException {
        AccessTokens at = Tokens.createAccessTokensWithUri(URI.create("http://we.use.filesystemsecrets.and.do.not.care.about.this"))
            .manageToken("noexistent")
                .addScope("read::all")
                .done()
            .start();
        TimeUnit.SECONDS.sleep(10);
        at.stop();
    }
    //@formatter:on

    //@formatter:off
    @Test
    public void testShouldThrowTokenMissingException() throws InterruptedException {
        try{
            Tokens.createAccessTokensWithUri(URI.create("http://we.use.filesystemsecrets.and.do.not.care.about.this"))
                  .manageToken("noexistent")
                      .addScope("read::all")
                      .done()
                  .manageToken("myfirst")
                      .addScope("read::all")
                      .done()
                  .manageToken("nonexistent-2")
                      .addScope("read::all")
                      .done()
                  .whenUsingFilesystemSecrets()
                      .validateTokensOnStartup()
                      .done()
                  .start();
            Assertions.fail("Exception expected");
        }catch(TokensMissingException e){
            Assertions.assertThat(e.getMessage()).startsWith("The following token-configurations couldn't be found : ");
            Assertions.assertThat(e.getMissingTokensCount()).isEqualTo(2);
        }
    }
    //@formatter:on

    //@formatter:off
    @Test
    public void testPolymorph() throws InterruptedException {
        AccessTokens at = Tokens.createAccessTokensWithUri(URI.create("http://we.use.filesystemsecrets.and.do.not.care.about.this"))
            .manageToken("noexistent")
                .addScope("read::all")
                .done()
            .start();
        Assertions.assertThat(at).isInstanceOfAny(AccessTokens.class, Secrets.class);

        TimeUnit.SECONDS.sleep(2);
        Assertions.assertThat(((Secrets)at).getClient("kio")).isNotNull();
        at.stop();
    }
    //@formatter:on
}
