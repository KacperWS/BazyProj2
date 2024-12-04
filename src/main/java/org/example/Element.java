package org.example;

public class Element {
    private final int key;
    private long offset;

    public Element(int key, long offset) {
        this.key = key;
        this.offset = offset;
    }

    public int getKey() {
        return key;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "Element{" +
                "key=" + key +
                ", offset=" + offset +
                '}';
    }
}
