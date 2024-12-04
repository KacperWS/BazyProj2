package org.example;

import java.util.ArrayList;
import java.util.List;

public class BTree {
    //private List<Node> tree;
    private Node root;
    private List<Node> path;
    private List<Long> deletedPages;
    private List<Long> deletedRecords;
    private final int pageSize;
    private final int treeCapacity;
    private Node current = null;    //d value

    public BTree(int treeCapacity) {
        this.pageSize = treeCapacity * 4 + 1;
        this.treeCapacity = treeCapacity;
        this.deletedRecords = new ArrayList<>();
        this.deletedPages = new ArrayList<>();
        //this.tree = new ArrayList<>();
        this.path = new ArrayList<>();
        Node root = new Node(treeCapacity);
        this.root = root;
        current = root;
        //tree.add(root);
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTreeCapacity() {
        return treeCapacity;
    }

    public Node getRoot() {
        return root;
    }

    public List<Long> getDeletedPages() {
        return deletedPages;
    }

    public void insert(int key, long offset) {
        search(key, root);
        if(current == null)
            return;
        if(current.getValues().size() < 2 * treeCapacity){
            insert2(current);
        }
        else if (current == root){
            Node temp = root;
            Node newRoot = new Node(treeCapacity);
            root = newRoot;
            newRoot.getChildren().add(temp);
            split();
            //insert2(newRoot);
        }else {
            if(compensate())
                return;
            else
                split();
            //insert2(root);
        }

        /*Node temp = root;
        if(root.getValues().size() ==  2 * treeCapacity){

            Node newRoot = new Node(treeCapacity);
            root = newRoot;
            newRoot.getChildren().add(temp);
            split();
            insert2(newRoot);
            //tree.set(0, newRoot);
            //newRoot.getValues().add();
            //int place = findPlace(root, key);
            //root.getValues().add(new Element(key, offset));
        }
        else {
            insert2(root);
        }*/
    }

    private void insert2(Node node) {

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

    public int find(int key, Node node){
        List<Element> temp = node.getValues();
        int i = 0;
        for(i = 0; i < temp.size(); i++){
            if(temp.get(i).getKey() >= key)
                return i;
        }
        return i;
    }

    public Element search(int key, Node s) {
        //Node s = root;
        path.add(root);
        if(s.getValues().isEmpty()) {
            current = root;
            return null;
        }
        else {
            int x = find(key, s);
            if(s.getValues().get(x).getKey() == key) {
                current = null;
                return s.getValues().get(x);
            }
            else {
                if(s.getPointers().isEmpty()) {
                    path.add(s);
                    current = s;
                }
                else {
                    path.add(s);
                    search(key, s.getChildren().get(x));
                }
            }
        }
        return null;
    }

    public boolean compensate() {
        Node parent = path.get(path.indexOf(current) - 1);
        List<Node> children = parent.getChildren();
        if (children.size() < 2)
            return false; //No sibling to compensate

        int index = children.indexOf(current);
        Node sibling;
        if(index < 1){
            if(children.)
        }
        //Node sibling = temp.getChildren().get(temp.getChildren().indexOf(current));
        return false;
    }

    public void split() {

    }

}
