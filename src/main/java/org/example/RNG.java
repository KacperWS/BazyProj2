package org.example;

import java.util.*;

public class RNG {
    private final long m = 1000000000;
    private final long a = (long) (Math.PI * 1000);
    private final long c = (long) (Math.exp(1) * 1000);
    private long x0;
    private long x;
    private Set<Long> test;

    public RNG(long seed) {
        this.x0 = seed;
        this.x = seed;
        test = new TreeSet<>();
    }

    public long random() {
        if(x != x0)
            test.add(x);
        return x = (a * x + c + new Random().nextLong(x0)) % m % 1000;
    }

    public int check() {
        long find = test.iterator().next();
        int counter = 0;
        for(int i = 1; i < test.size(); i++){
            if(test.iterator().next() != find)
                counter++;
            else break;
        }
        return test.size();
    }

    public long value() {
        return test.iterator().next();
    }

}
