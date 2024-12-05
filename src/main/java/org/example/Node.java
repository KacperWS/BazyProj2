package org.example;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private List<Element> values;
    private List<Long> pointers;
    private List<Node> children;
    private long number;


    public Node(int d) {
        this.values = new ArrayList<>(2 * d);
        this.pointers = new ArrayList<>(2 * d + 1);
        this.children = new ArrayList<>(2 * d + 1);
    }

    public List<Element> getValues() {
        return values;
    }

    public List<Long> getPointers() {
        return pointers;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setPointers(List<Long> pointers) {
        this.pointers = pointers;
    }

    public void setValues(List<Element> values) {
        this.values = values;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public void set(List<Element> values, List<Long> pointers) {
        this.pointers = pointers;
        this.values = values;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    @Override
    public String toString() {
        String t="";
        for (int i = 0; i < values.size(); i++)
            t+= values.get(i).toString() + " ";
        return t;
    }
}
