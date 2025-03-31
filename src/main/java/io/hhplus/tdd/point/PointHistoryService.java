package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PointHistoryService {

    private final PointHistoryTable pointHistoryTable;

    public List<PointHistory> getPointHistories(long userId) {

        return Optional
                .ofNullable(pointHistoryTable.selectAllByUserId(userId))
                .orElseGet(Collections::emptyList);
    }
}
