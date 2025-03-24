package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserPointChargeService {

    private final UserPointTable userPointTable;

    private final PointHistoryTable pointHistoryTable;

    public UserPoint chargePoint(long userId, long amount) {

        UserPoint userPoint = userPointTable.selectById(userId);

        if (userPoint == null) {
            userPoint = UserPoint.empty(userId);
        }

        UserPoint chargedUserPoint = userPoint.plusPoint(amount);
        long chargedPoint = chargedUserPoint.point();

        UserPoint addedUserPoint = userPointTable.insertOrUpdate(userId, chargedPoint);
        pointHistoryTable.insert(userId, chargedPoint, TransactionType.CHARGE, System.currentTimeMillis());

        return addedUserPoint;
    }

}
