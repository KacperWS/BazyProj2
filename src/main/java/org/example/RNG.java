package org.example;

import java.util.*;

public class RNG {
    private final long m = Long.MAX_VALUE;
    private final long a = (long) (Math.PI * 10000);
    private final long c = (long) (Math.exp(1) * 10000);
    private long x0;
    private long x;
    private final Set<Long> test;

    public RNG(long seed) {
        this.x0 = seed;
        this.x = seed;
        test = new TreeSet<>();
    }

    public long random() {
        test.add(x);
        return x = (a * x + c + new Random().nextLong(x0)) % m % x0;
    }

    public int check() {
        return test.size();
    }

    public long value() {
        return test.iterator().next();
    }

    public long getX() {
        return x;
    }

    public void changeSeed(long seed) {
        this.x0 = seed;
        this.x = seed;
    }
}
