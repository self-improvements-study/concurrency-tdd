package io.hhplus.tdd.point.charge;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SynchronizedUserPointChargeService implements UserPointChargeService {

    private final UserPointTable userPointTable;

    private final PointHistoryTable pointHistoryTable;

    @Override
    public UserPoint chargePoint(long userId, long amount) {
        UserPoint saved;

        synchronized (this) {
            UserPoint userPoint = userPointTable.selectById(userId);

            if (userPoint == null) {
                userPoint = UserPoint.empty(userId);
            }

            UserPoint chargedUserPoint = userPoint.plusPoint(amount);
            long chargedPoint = chargedUserPoint.point();

            saved = userPointTable.insertOrUpdate(userId, chargedPoint);
        }

        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return saved;
    }

}
