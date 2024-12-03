package org.example;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private List<Element> values;
    private List<Long> pointers;

    public Node(int d) {
        this.values = new ArrayList<>(d);
        this.pointers = new ArrayList<>(d + 1);
    }
}
