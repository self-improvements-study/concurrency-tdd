package io.hhplus.tdd.point.charge;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Primary
@RequiredArgsConstructor
@Service
public class LockedUserPointChargeService implements UserPointChargeService {

    private final UserPointTable userPointTable;

    private final PointHistoryTable pointHistoryTable;

    private final Map<Long, Lock> userLockMap = new ConcurrentHashMap<>();

    private Lock getLockUser(long userId) {
        return userLockMap.computeIfAbsent(userId, value -> new ReentrantLock(true));
    }

    @Override
    public UserPoint chargePoint(long userId, long amount) {
        Lock lock = getLockUser(userId);
        lock.lock();

        try {
            UserPoint userPoint = userPointTable.selectById(userId);

            if (userPoint == null) {
                userPoint = UserPoint.empty(userId);
            }

            UserPoint chargedUserPoint = userPoint.plusPoint(amount);
            long chargedPoint = chargedUserPoint.point();

            UserPoint saved = userPointTable.insertOrUpdate(userId, chargedPoint);

            pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return saved;
        } finally {
            lock.unlock();
        }
    }

}
