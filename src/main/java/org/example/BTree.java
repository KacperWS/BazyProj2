package org.example;

import java.util.List;

public class BTree {
    private List<Node> tree;
    private long[] deletedPages;
    private long[] deletedRecords;
    private int pageSize;
    private int treeCapacity; //d value

    public BTree(int pageSize, int treeCapacity) {
        this.pageSize = pageSize;
        this.treeCapacity = treeCapacity;
    }
}
