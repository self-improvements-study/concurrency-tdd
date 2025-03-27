package io.hhplus.tdd.point.charge;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointHistoryService;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.UserPointFindService;
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
class LockedUserPointChargeServiceIntegrationTest {

    @Autowired
    private LockedUserPointChargeService sut;

    @Autowired
    private UserPointFindService userPointFindService;
    @Autowired
    private PointHistoryService pointHistoryService;

    @DisplayName("동시성 환경에서 포인트 충전 성공")
    @Test
    void success() {

        // given
        int threadCount = 30;
        long userId = 1L;
        long amount = 3L;

        // when
        ConcurrencyTestUtil.executeConcurrency(threadCount, () -> sut.chargePoint(userId, amount));
        // then
        UserPoint userPoint = userPointFindService.getPointById(userId);
        assertThat(userPoint)
                .isNotNull()
                .returns(amount * threadCount, UserPoint::point);

        List<PointHistory> histories = pointHistoryService.getPointHistories(userId);
        assertThat(histories)
                .isNotNull()
                .isNotEmpty()
                .hasSize(threadCount)
                .allMatch(history -> history.amount() == amount);

    }

    @DisplayName("동시성 환경에서 포인트 충전 실패 - 최대 포인트 초과")
    @Test
    void failure1() {

        // given
        int threadCount = 10;
        long userId = 1L;
        long amount = 20L;
        long maxPoints = 100L;

        // when

        try (ExecutorService executorService = Executors.newFixedThreadPool(threadCount)) {
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        sut.chargePoint(userId, amount);
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
                    assertThat(point.point()).isLessThanOrEqualTo(maxPoints);
                });

        List<PointHistory> histories = pointHistoryService.getPointHistories(userId);
        assertThat(histories)
                .isNotNull()
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(5);
    }

}
