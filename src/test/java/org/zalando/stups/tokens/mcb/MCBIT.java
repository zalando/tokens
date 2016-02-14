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
package org.zalando.stups.tokens.mcb;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.Mockito;

public class MCBIT {

    @Test
    public void scenario() throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AnService anService = Mockito.mock(AnService.class);
        initializeServiceBehaviour(anService);
        scheduler.scheduleAtFixedRate(new Runner(new MCB(), anService), 2, 3, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(500);
    }


    static class Runner implements Runnable {

        private final MCB mcb;
        private final AnService anService;

        public Runner(MCB mcb, AnService anService) {
            this.mcb = mcb;
            this.anService = anService;
        }

        @Override
        public void run() {
            if (mcb.isClosed()) {
                System.out.println("DOING SOMETHING");
                try {
                    anService.invoke();
                    mcb.onSuccess();
                    System.out.println("-- ALL FINE");
                } catch (Throwable t) {
                    System.out.println("-- OOPS, THROWABLE");
                    // cb.newThrowable(t);
                    mcb.onError();
                }

            }
        }

    }

    //@formatter:off
    private void initializeServiceBehaviour(AnService anService) {
        Mockito.when(anService.invoke()).thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            // to open
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
            // now half-open
            // to open again
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
            // now half-open
            // to open again
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
            // now half-open
            // to closed
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            // to open
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
                .thenThrow(new RuntimeException("TEST_EXCEPTION"))
            // now half-open
            // now fine all the time
            .thenReturn(true)
            .thenReturn(true);

    }
    //formatter:on
}
