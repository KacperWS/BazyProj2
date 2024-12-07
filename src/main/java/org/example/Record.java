package org.example;

import java.util.Arrays;

public class Record {

    private long id;
    private int[] data;

    public Record(long id, int[] data) {
        this.data = data;
        this.id = id;
    }

    public Record() {
        this.data = new int[6];
    }

    public int[] getData() {
        return data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
