package com.epam.lenda.gymapp.util;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class MapBasedStorage<T> implements Cloneable {
    @Getter
    private final Map<Long, T> data;
    private long nextId;

    public MapBasedStorage() {
        data = new HashMap<>();
        nextId = 1L;
    }

    public MapBasedStorage(Map<Long, T> data) {
        this.data = new HashMap<>(data);
        refreshNextId();
    }

    public long nextId() {
        return nextId++;
    }

    public long peekId() {
        return nextId;
    }

    public void refreshNextId() {
        this.nextId = data.keySet().stream().mapToLong(id -> id + 1).max().orElse(1L);
    }

    @Override
    public MapBasedStorage<T> clone() {
        return new MapBasedStorage<>(data);
    }
}
