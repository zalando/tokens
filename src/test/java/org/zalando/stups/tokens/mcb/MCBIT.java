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
