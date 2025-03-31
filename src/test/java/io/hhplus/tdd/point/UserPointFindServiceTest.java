package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPointFindServiceTest {

    @InjectMocks
    private UserPointFindService sut;

    @Mock
    private UserPointTable userPointTable;

    @DisplayName("유저 포인트 조회 성공")
    @Test
    void success() {

        // given
        long id = 1L;
        when(userPointTable.selectById(id)).thenReturn(UserPoint.empty(id));

        // when
        UserPoint pointById = sut.getPointById(id);

        // then
        assertThat(pointById)
                .isNotNull()
                .returns(id, UserPoint::id);
    }

    @DisplayName("유저 포인트 조회 실패 - 존재하지 않는 유저")
    @Test
    void failure() {

        // given
        long id = 1L;
        when(userPointTable.selectById(id)).thenReturn(null);

        // when

        // then
        assertThatThrownBy(() -> sut.getPointById(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("not found");
    }

}