package ru.spbau.mit;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public class LockHandler implements AutoCloseable {

    private static final Queue<LockHandler> CACHE = new ArrayDeque<>();
    private Lock lock;

    public static LockHandler lock(Lock lock) {
        LockHandler result;
        synchronized (CACHE) {
            result = CACHE.poll();
        }
        if (result == null) {
            result = new LockHandler();
        }
        result.lock = lock;
        lock.lock();
        return result;
    }

    @Override
    public void close() {
        lock.unlock();
        synchronized (CACHE) {
            CACHE.add(this);
        }
    }
}
