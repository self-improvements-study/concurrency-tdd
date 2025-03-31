package io.hhplus.tdd.point.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrencyTestUtil {

    public static void executeConcurrency(int threadCount, Runnable runnable) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        runnable.run();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
