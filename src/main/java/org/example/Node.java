package org.example;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private List<Element> values;
    private List<Long> pointers;


    public Node(int d) {
        this.values = new ArrayList<>(2 * d);
        this.pointers = new ArrayList<>(2 * d + 1);
    }

    public List<Element> getValues() {
        return values;
    }

    public List<Long> getPointers() {
        return pointers;
    }

    public void setPointers(List<Long> pointers) {
        this.pointers = pointers;
    }

    public void setValues(List<Element> values) {
        this.values = values;
    }

    public void set(List<Element> values, List<Long> pointers) {
        this.pointers = pointers;
        this.values = values;
    }
}
