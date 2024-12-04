package org.example;

import java.util.ArrayList;
import java.util.Comparator;
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
            split(key, offset);
            //insert2(newRoot);
        }else {
            /*if(compensate())
                return;
            else
                split();*/
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

    public boolean compensate(int key, long offset) { //Only possible in leaf
        Node parent = path.get(path.indexOf(current) - 1);
        List<Node> children = parent.getChildren();
        if (children.size() < 2)
            return false; //No siblings to compensate

        int index = children.indexOf(current);
        if(index < 1){ //Right sibling should exist
            if(children.get(1).getValues().size() == treeCapacity * 2)
                return false; //sibling is full
            Node siblingRight = children.get(1);
            Element newOne = new Element(key, offset);
            List<Element> temp = siblingRight.getValues();
            temp.add(parent.getValues().get(index));
            temp.addAll(children.get(index).getValues());
            temp.add(newOne); //create temp list with all
            temp.sort(Comparator.comparingInt(Element::getKey));
            int middleValue = (int) Math.round(temp.size()/2.0 - 1);
            Element middle = temp.get(middleValue); //choose middle

            parent.getValues().set(index - 1, middle); // set parent to middle
            current.getValues().clear();
            List<Element> tempList = current.getValues(); //redistribute equally
            for(int i = 0; i < middleValue; i++){
                tempList.add(temp.get(i));
            }
            siblingRight.getValues().clear();
            tempList = siblingRight.getValues();
            for(int i = middleValue + 1; i < temp.size(); i++){
                tempList.add(temp.get(i));
            }
            return true;
        }
        else {
            Node siblingLeft = children.get(index - 1);
            if(!(siblingLeft.getValues().size() == treeCapacity * 2)) {
                //sibling has space
                Element newOne = new Element(key, offset);
                List<Element> temp = siblingLeft.getValues();
                temp.add(parent.getValues().get(index - 1));
                temp.addAll(children.get(index).getValues());
                temp.add(newOne); //create temp list with all
                temp.sort(Comparator.comparingInt(Element::getKey));
                int middleValue = (int) Math.round(temp.size()/2.0 - 1);
                Element middle = temp.get(middleValue); //choose middle

                parent.getValues().set(index - 1, middle); // set parent to middle
                current.getValues().clear();
                List<Element> tempList = current.getValues(); //redistribute equally
                for(int i = 0; i < middleValue; i++){
                    tempList.add(temp.get(i));
                }
                siblingLeft.getValues().clear();
                tempList = siblingLeft.getValues();
                for(int i = middleValue + 1; i < temp.size(); i++){
                    tempList.add(temp.get(i));
                }
                return true;
            }

            Node siblingRight;
            if(children.size() - 1 > index)
                siblingRight = children.get(index + 1); //right sibling should exist
            else
                return false; //No siblings to match

            if(siblingRight.getValues().size() == treeCapacity * 2)
                return false; //sibling is full

            //compensate with right
            //Node siblingRight = children.get(1);
            Element newOne = new Element(key, offset);
            List<Element> temp = siblingRight.getValues();
            temp.add(parent.getValues().get(index));
            temp.addAll(children.get(index).getValues());
            temp.add(newOne); //create temp list with all
            temp.sort(Comparator.comparingInt(Element::getKey));
            int middleValue = (int) Math.round(temp.size()/2.0 - 1);
            Element middle = temp.get(middleValue); //choose middle

            parent.getValues().set(index - 1, middle); // set parent to middle
            current.getValues().clear();
            List<Element> tempList = current.getValues(); //redistribute equally
            for(int i = 0; i < middleValue; i++){
                tempList.add(temp.get(i));
            }
            siblingRight.getValues().clear();
            tempList = siblingRight.getValues();
            for(int i = middleValue + 1; i < temp.size(); i++){
                tempList.add(temp.get(i));
            }
            return true;
        }
        //Node sibling = temp.getChildren().get(temp.getChildren().indexOf(current));
        //return false;
    }

    public void split(int key, long offset) {
        Node parent = path.get(path.indexOf(current) - 1);
        List<Node> children = parent.getChildren();
        Node newNode = new Node(treeCapacity);

        if(current.getPointers().isEmpty()) { //Code below to add to leaf
            Element newOne = new Element(key, offset);

            current.getValues().add(newOne);
            current.getValues().sort(Comparator.comparingInt(Element::getKey));

            int middleValue = (int) Math.round(current.getValues().size() / 2.0 - 1);
            List<Element> tempList; //redistribute equally

            List<Element> temp = current.getValues();
            tempList = new ArrayList<>(temp.subList(middleValue + 1, temp.size()));
            newNode.setValues(tempList);
            List<Element> tempList1;
            tempList1 = new ArrayList<>(temp.subList(0, middleValue - 1));
            Element middle = temp.get(middleValue);
            temp.clear();
            temp.addAll(tempList1);

            int index = children.indexOf(current);
            parent.getChildren().add(index + 1, newNode);
            parent.getValues().add(index, middle);
        }
        else {
            int middleValue = (int) Math.round(current.getValues().size() / 2.0 - 1);
            List<Element> tempList; //redistribute equally

            List<Element> temp = current.getValues();
            tempList = new ArrayList<>(temp.subList(middleValue + 1, temp.size()));
            newNode.setValues(tempList);
            List<Element> tempList1;
            tempList1 = new ArrayList<>(temp.subList(0, middleValue - 1));
            Element middle = temp.get(middleValue);
            temp.clear();
            temp.addAll(tempList1);

            int index = children.indexOf(current);
            parent.getChildren().add(index + 1, newNode);
            parent.getValues().add(index, middle);

            newNode.setPointers(new ArrayList<>(current.getPointers().subList(middleValue + 1, current.getPointers().size())));
            newNode.setChildren(new ArrayList<>(current.getChildren().subList(middleValue + 1, current.getChildren().size())));

            current.setPointers(new ArrayList<>(current.getPointers().subList(0, middleValue)));
            current.setChildren(new ArrayList<>(current.getChildren().subList(0, middleValue)));

        }
    }

}
