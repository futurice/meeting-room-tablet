package com.futurice.android.reservator.common;

import java.util.HashMap;

public class CacheMap<K, V> {
    private final HashMap<K, Bucket> map;

    public CacheMap() {
        this.map = new HashMap<K, Bucket>();
    }

    public V get(K key) {
        Bucket b = map.get(key);

        if (b == null || b.getExpireMillis() < System.currentTimeMillis()) {
            return null;
        } else {
            return b.getValue();
        }
    }

    public void put(K key, V value, long duration) {
        map.put(key, new Bucket(value, System.currentTimeMillis() + duration));
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void clear() {
        this.map.clear();
    }

    private class Bucket {
        private long expireAt;
        private V value;

        public Bucket(V value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        public V getValue() {
            return value;
        }

        public long getExpireMillis() {
            return expireAt;
        }
    }
}
