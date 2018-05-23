package com.zoe.framework.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LRU缓存链表，适用于读多于写的情况
 *
 * Created by caizhicong on 2015/11/25.
 */
public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private int capacity;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final long serialVersionUID = 1L;
    //private final Lock lock = new ReentrantLock();
    private final Lock rl;
    private final Lock wl;

    public LRULinkedHashMap(int capacity) {
        super((int) Math.ceil(capacity / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, true);
        this.capacity = capacity;

        ReadWriteLock rrwl = new ReentrantReadWriteLock();
        rl = rrwl.readLock();
        wl = rrwl.writeLock();
    }

    @Override
    public boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    @Override
    public V get(Object key)
    {
        try {
            rl.lock();
            return super.get(key);
        }
        finally {
            rl.unlock();
        }
    }

    @Override
    public V put(K key, V value)
    {
        try {
            wl.lock();
            return super.put(key, value);
        }
        finally {
            wl.unlock();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, V> entry : entrySet()) {
            sb.append(String.format("%s:%s ", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }
}
