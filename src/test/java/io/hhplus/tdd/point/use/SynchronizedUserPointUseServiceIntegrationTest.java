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

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class SynchronizedUserPointUseServiceIntegrationTest {

    @Autowired
    private SynchronizedUserPointUseService sut;

    @Autowired
    private LockedUserPointChargeService chargeService;

    @Autowired
    private UserPointFindService userPointFindService;
    @Autowired
    private PointHistoryService pointHistoryService;

    @DisplayName("동시성 환경에서 포인트 충전 성공")
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
}