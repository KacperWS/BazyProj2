package org.example;

import java.util.ArrayList;
import java.util.List;

public class BTree {
    private List<Node> tree;
    private List<Long> deletedPages;
    private List<Long> deletedRecords;
    private final int pageSize;
    private final int treeCapacity; //d value

    public BTree(int treeCapacity) {
        this.pageSize = treeCapacity * 4 + 1;
        this.treeCapacity = treeCapacity;
        this.deletedRecords = new ArrayList<>();
        this.deletedPages = new ArrayList<>();
        this.tree = new ArrayList<>();
        Node root = new Node(treeCapacity);
        tree.add(root);
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTreeCapacity() {
        return treeCapacity;
    }

    public List<Node> getTree() {
        return tree;
    }

    public List<Long> getDeletedPages() {
        return deletedPages;
    }

    public void insert(int key, long offset) {
        Node root = tree.getFirst();
        if(root.getValues().size() ==  2 * treeCapacity){
            Node newRoot = new Node(treeCapacity);
            tree.set(0, newRoot);
            newRoot.getValues().add();
            //int place = findPlace(root, key);
            //root.getValues().add(new Element(key, offset));
        }
        else {

        }
    }

    public int findPlace(Node node, int key){
        List<Element> temp = node.getValues();
        for(int i = 0; i < node.getValues().size(); i++){
            if(temp.get(i).getKey() > key){
                return i;
            }
        }
        return 0;
    }

    public void search() {

    }

    public void compensate() {

    }

    public void split() {

    }

}
