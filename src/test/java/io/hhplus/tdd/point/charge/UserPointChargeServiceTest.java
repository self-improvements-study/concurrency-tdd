package io.hhplus.tdd.point.charge;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPointChargeServiceTest {

    @InjectMocks
    private LockedUserPointChargeService sut;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @DisplayName("포인트 충전 성공")
    @ParameterizedTest
    @ValueSource(longs = {10, 20, 30, 40, 50, 60, 70, 80, 90})
    void success(long amount) {

        // given
        long currentPoint = 10L;
        long userId = 1L;
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(eq(userId), eq(amount + currentPoint)))
                .thenReturn(new UserPoint(userId, amount + currentPoint, System.currentTimeMillis()));
        when(pointHistoryTable.insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong()))
                .thenReturn(new PointHistory(userId, userId, amount, TransactionType.CHARGE, System.currentTimeMillis()));

        // when
        UserPoint userPoint = sut.chargePoint(userId, amount);

        // then
        assertThat(userPoint)
                .isNotNull()
                .returns(currentPoint + amount, UserPoint::point);
    }

    @DisplayName("포인트 충전 실패 - 충전 금액이 0 이하일 때")
    @Test
    void failure1() {

        // given
        long userId = 1L;
        long amount = 0;

        // when

        // then
        assertThatThrownBy(() -> sut.chargePoint(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be greater than 0");
    }

    @DisplayName("포인트 충전 실패 - 최대 포인트 초과")
    @Test
    void failure2() {

        // given
        long userId = 1L;
        long currentPoint = 100L;
        long amount = 1L;
        long addedPoint = currentPoint + amount;

        // when
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, addedPoint, System.currentTimeMillis()));

        // then
        assertThatThrownBy(() -> sut.chargePoint(userId, amount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("not enough point");
    }

}