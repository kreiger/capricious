package com.linuxgods.kreiger.util;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

public class ExpiringQueue<T> extends AbstractQueue<T> {
    private final Queue<Expiring<T>> delayQueue = new ArrayDeque<>();
    private final Duration expiration;

    public ExpiringQueue(Duration expiration) {
        this.expiration = expiration;
    }

    @Override
    public Iterator<T> iterator() {
        return delayQueue.stream().map(Supplier::get).iterator();
    }

    @Override
    public int size() {
        return delayQueue.size();
    }

    @Override
    public boolean offer(T o) {
        return delayQueue.offer(new Expiring<>(o, Instant.now().plus(expiration)));
    }

    @Override
    public T poll() {
        Expiring<T> expiring = delayQueue.poll();
        return null == expiring ? null : expiring.get();
    }

    @Override
    public T peek() {
        Expiring<T> expiring = delayQueue.peek();
        return null == expiring ? null : expiring.get();
    }

    public void removeExpired() {
        removeExpiredUntil(Instant.now());
    }

    private void removeExpiredUntil(Instant now) {
        for (Iterator<Expiring<T>> iterator = delayQueue.iterator(); iterator.hasNext(); ) {
            Expiring<T> next = iterator.next();
            if (now.isBefore(next.expiry)) {
                break;
            }
            iterator.remove();
            onRemovedExpired(next.get());
        }
    }

    public void onRemovedExpired(T expired) {
    }

    private class Expiring<T> implements Supplier<T> {
        private final T object;
        private final Instant expiry;

        private Expiring(T object, Instant expiry) {
            this.object = object;
            this.expiry = expiry;
        }

        @Override
        public T get() {
            return object;
        }
    }
}
