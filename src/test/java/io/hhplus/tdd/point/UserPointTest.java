package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserPointTest {

    @DisplayName("포인트 추가 성공")
    @Test
    void plusPointTestSuccess() {

        // given
        UserPoint userPoint = new UserPoint(1L, 10L, System.currentTimeMillis());

        // when
        UserPoint currentPoint = userPoint.plusPoint(10L);

        // then
        assertThat(currentPoint)
                .isNotNull()
                .returns(20L, UserPoint::point);

    }

    @DisplayName("포인트 추가 실패 - 추가할 금액이 0일 경우 예외 발생")
    @Test
    void plusPointTestFailure1() {

        // given
        UserPoint userPoint = new UserPoint(1L, 10L, System.currentTimeMillis());

        // when

        // then
        assertThatThrownBy(() -> userPoint.plusPoint(0L))
                .hasMessage("amount must be greater than 0");

    }

    @DisplayName("포인트 추가 실패 - 보유 포인트보다 많은 금액을 추가하려 할 경우 예외 발생")
    @Test
    void plusPointTestFailure2() {

        // given
        UserPoint userPoint = new UserPoint(1L, 10L, System.currentTimeMillis());

        // when

        // then
        assertThatThrownBy(() -> userPoint.plusPoint(100L))
                .hasMessage("not enough point");

    }

    @DisplayName("포인트 사용 성공")
    @Test
    void minusPointTestSuccess() {

        // given
        UserPoint userPoint = new UserPoint(1L, 10L, System.currentTimeMillis());

        // when
        UserPoint currentPoint = userPoint.minusPoint(10L);

        // then
        assertThat(currentPoint)
                .isNotNull()
                .returns(0L, UserPoint::point);

    }

    @DisplayName("포인트 사용 실패 - 사용 금액이 0일 경우 예외 발생")
    @Test
    void minusPointTestFailure1() {

        // given
        UserPoint userPoint = new UserPoint(1L, 10L, System.currentTimeMillis());

        // when

        // then
        assertThatThrownBy(() -> userPoint.minusPoint(0L))
                .hasMessage("amount must be greater than 0");

    }

    @DisplayName("포인트 사용 실패 - 보유 포인트보다 많은 금액을 사용하려 할 경우 예외 발생")
    @Test
    void minusPointTestFailure2() {

        // given
        UserPoint userPoint = new UserPoint(1L, 10L, System.currentTimeMillis());

        // when

        // then
        assertThatThrownBy(() -> userPoint.minusPoint(100L))
                .hasMessage("not enough point");

    }
}