package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    @InjectMocks
    private PointHistoryService sut;

    @Mock
    private PointHistoryTable pointHistoryTable;


    @DisplayName("사용자가 충전 혹은 사용 내역을 가지고 있을 때")
    @Test
    void success1() {

        // given
        long userId = 1L;
        when(pointHistoryTable.selectAllByUserId(userId))
                .thenReturn(List.of(new PointHistory(1L, userId, 100L, TransactionType.CHARGE, System.currentTimeMillis())));

        // when
        List<PointHistory> pointHistoryList = sut.getPointHistories(userId);

        // then
        assertThat(pointHistoryList).hasSize(1);
    }

    @DisplayName("사용자가 충전 혹은 사용 내역이 없을 때")
    @Test
    void success2() {

        // given
        long userId = 1L;
        when(pointHistoryTable.selectAllByUserId(userId))
                .thenReturn(null);

        // when
        List<PointHistory> pointHistoryList = sut.getPointHistories(userId);

        // then
        assertThat(pointHistoryList).isEmpty();
    }

}