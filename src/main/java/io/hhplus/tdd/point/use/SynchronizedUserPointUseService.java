package io.hhplus.tdd.point.use;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SynchronizedUserPointUseService implements UserPointUseService {

    private final UserPointTable userPointTable;

    private final PointHistoryTable pointHistoryTable;

    @Override
    public UserPoint usePoint(long userId, long amount) {
        UserPoint saved;

        synchronized (this) {
            UserPoint userPoint = userPointTable.selectById(userId);

            if (userPoint == null) {
                userPoint = UserPoint.empty(userId);
            }

            UserPoint usedUserPoint = userPoint.minusPoint(amount);
            long usedPoint = usedUserPoint.point();

            saved = userPointTable.insertOrUpdate(userId, usedPoint);
        }

        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

        return saved;
    }

}
