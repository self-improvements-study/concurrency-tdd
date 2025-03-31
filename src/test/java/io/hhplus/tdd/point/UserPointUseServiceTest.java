package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
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
class UserPointUseServiceTest {

    @InjectMocks
    private UserPointUseService sut;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @DisplayName("포인트 사용 성공")
    @ParameterizedTest
    @ValueSource(longs = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100})
    void success(long amount) {

        // given
        long userId = 1L;

        long currentPoint = 100L;
        long remainingPoint = currentPoint - amount;

        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, currentPoint, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(eq(userId), eq(remainingPoint)))
                .thenReturn(new UserPoint(userId, remainingPoint, System.currentTimeMillis()));
        when(pointHistoryTable.insert(eq(userId), eq(amount), eq(TransactionType.USE), anyLong()))
                .thenReturn(new PointHistory(userId, userId, amount, TransactionType.USE, System.currentTimeMillis()));

        // when
        UserPoint userPoint = sut.usePoint(userId, amount);

        // then
        assertThat(userPoint)
                .isNotNull()
                .returns(remainingPoint, UserPoint::point);
    }

    @DisplayName("포인트 사용 실패 - 사용 금액이 0 또는 음수일 경우")
    @Test
    void failure1() {

        // given
        long userId = 1L;
        long amount = 0;

        // when

        // then
        assertThatThrownBy(() -> sut.usePoint(userId, amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be greater than 0");
    }

    @DisplayName("포인트 사용 실패 - 잔액 부족할 경우")
    @Test
    void failure2() {

        // given
        long userId = 1L;
        long currentPoint = 1L;
        long amount = 2L;
        long remainingPoint = currentPoint - amount;

        // when
        when(userPointTable.selectById(userId))
                .thenReturn(new UserPoint(userId, remainingPoint, System.currentTimeMillis()));

        // then
        assertThatThrownBy(() -> sut.usePoint(userId, amount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("not enough point");
    }
}