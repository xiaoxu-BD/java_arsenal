package org.xiaoxu.patterns.advanced.decorator;

import java.util.HashMap;
import java.util.Map;

/** 基础实现：内存存储。只做存取这一件事，别的一概不管（单一职责） */
public class MemoryStorage implements Storage {

    private final Map<String, String> store = new HashMap<>();

    @Override
    public void save(String key, String value) {
        store.put(key, value);
    }

    @Override
    public String read(String key) {
        return store.get(key);
    }
}
