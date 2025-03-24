package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserPointUseService {

    private final UserPointTable userPointTable;

    private final PointHistoryTable pointHistoryTable;

    public UserPoint usePoint(long userId, long amount) {

        UserPoint userPoint = userPointTable.selectById(userId);

        if (userPoint == null) {
            userPoint = UserPoint.empty(userId);
        }

        UserPoint currentUserPoint = userPoint.minusPoint(amount);
        long remainingPoint = currentUserPoint.point();

        UserPoint remainingUserPoint = userPointTable.insertOrUpdate(userId, remainingPoint);

        pointHistoryTable.insert(userId, remainingPoint, TransactionType.USE, System.currentTimeMillis());

        return remainingUserPoint;
    }
}
