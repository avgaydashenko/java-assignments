package ru.spbau.mit;

public final class Id {
    private static Id instance;
    private int currentId = 1;

    public static synchronized Id getInstance() {
        if (instance == null) {
            instance = new Id();
        }
        return instance;
    }

    public synchronized int nextId() {
        return currentId++;
    }

    public synchronized void reset() {
        currentId = 1;
    }
}
