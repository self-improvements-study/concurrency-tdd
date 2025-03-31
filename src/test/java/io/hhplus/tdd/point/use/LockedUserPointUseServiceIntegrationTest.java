package io.hhplus.tdd.point.use;

import io.hhplus.tdd.point.*;
import io.hhplus.tdd.point.charge.LockedUserPointChargeService;
import io.hhplus.tdd.point.util.ConcurrencyTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class LockedUserPointUseServiceIntegrationTest {

    @Autowired
    private LockedUserPointUseService sut;

    @Autowired
    private LockedUserPointChargeService chargeService;

    @Autowired
    private UserPointFindService userPointFindService;
    @Autowired
    private PointHistoryService pointHistoryService;

    @DisplayName("동시성 환경에서 포인트 사용 성공")
    @Test
    void success() {

        // given
        int threadCount = 10;
        long userId = 1L;
        long currentPoint = 100L;
        long amount = 5L;

        chargeService.chargePoint(userId, currentPoint);

        // when
        ConcurrencyTestUtil.executeConcurrency(threadCount, () -> sut.usePoint(userId, amount));

        // then
        UserPoint userPoint = userPointFindService.getPointById(userId);
        assertThat(userPoint)
                .isNotNull()
                .returns(amount * threadCount, UserPoint::point);

        List<PointHistory> histories = pointHistoryService.getPointHistories(userId);
        assertThat(histories)
                .isNotNull()
                .isNotEmpty()
                .hasSize(threadCount + 1)
                .filteredOn(history -> history.type() == TransactionType.USE)
                .allMatch(history -> history.amount() == amount);
    }

    @DisplayName("동시성 환경에서 포인트 사용 실패 - 잔액 부족할 경우")
    @Test
    void failure1() {

        // given
        int threadCount = 10;
        long userId = 1L;
        long currentPoint = 100L;
        long amount = 20L;

        chargeService.chargePoint(userId, currentPoint);

        // when
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        sut.usePoint(userId, amount);
                    } catch (IllegalStateException e) {
                        assertThat(e.getMessage()).isEqualTo("not enough point");
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // then
        UserPoint userPoint = userPointFindService.getPointById(userId);
        assertThat(userPoint)
                .isNotNull()
                .satisfies(point -> {
                    assertThat(point.point()).isLessThanOrEqualTo(0);
                });

        List<PointHistory> histories = pointHistoryService.getPointHistories(userId);
        assertThat(histories)
                .isNotNull()
                .isNotEmpty()
                .filteredOn(history -> history.type() == TransactionType.USE)
                .hasSizeLessThanOrEqualTo(5);
    }
}