package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static final long MAX_POINT = 100L;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint plusPoint(long amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        long addedPoint = point + amount;

        if (addedPoint > MAX_POINT) {
            throw new IllegalStateException("not enough point");
        }
        return new UserPoint(id, addedPoint, System.currentTimeMillis());
    }

    public UserPoint minusPoint(long amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }

        long remainingPoint = point - amount;

        if (remainingPoint < 0) {
            throw new IllegalStateException("not enough point");
        }
        return new UserPoint(id, remainingPoint, System.currentTimeMillis());
    }
}
