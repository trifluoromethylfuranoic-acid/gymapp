package com.epam.lenda.gymapp.util;

import com.epam.lenda.gymapp.model.HasId;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public class MapBasedStorage<T extends HasId> implements Cloneable {
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

    public void refreshNextId() {
        this.nextId = data.keySet().stream().mapToLong(id -> id + 1).max().orElse(1L);
    }

    public void save(T entity) {
        if (entity.getId() == null) {
            persist(entity);
        } else {
            merge(entity);
        }
    }

    public void persist(T entity) {
        final var id = nextId();
        entity.setId(id);
        data.put(id, entity);
    }

    public void merge(T entity) {
        data.put(entity.getId(), entity);
    }

    @Override
    public MapBasedStorage<T> clone() {
        return new MapBasedStorage<>(data);
    }
}
