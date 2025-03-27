package io.hhplus.tdd.point.use;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Primary
@RequiredArgsConstructor
@Service
public class LockedUserPointUseService implements UserPointUseService {

    private final UserPointTable userPointTable;

    private final PointHistoryTable pointHistoryTable;

    private final ConcurrentHashMap<Long, Lock> userLockMap = new ConcurrentHashMap<>();

    private Lock getLockUser(long userId) {
        return userLockMap.computeIfAbsent(userId, value -> new ReentrantLock(true));
    }

    @Override
    public UserPoint usePoint(long userId, long amount) {
        Lock lock = getLockUser(userId);
        lock.lock();

        try {
            UserPoint userPoint = userPointTable.selectById(userId);

            if (userPoint == null) {
                userPoint = UserPoint.empty(userId);
            }

            UserPoint currentUserPoint = userPoint.minusPoint(amount);
            long remainingPoint = currentUserPoint.point();

            UserPoint saved = userPointTable.insertOrUpdate(userId, remainingPoint);

            pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

            return saved;
        } finally {
            lock.unlock();
        }
    }
}
