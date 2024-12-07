package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BTree {
    //private List<Node> tree;
    private Node root;
    private final List<Node> path;
    private final List<Node> pathCopy;
    private final List<Integer> deletedPages;
    private final List<Long> deletedRecords;
    private final int pageSize;
    private final int treeCapacity; //d value
    private Node current;
    private final DiscIO disc;
    private int pageNumber = 0; //first free page number to use
    private long offset = 0;

    public BTree(int treeCapacity) {
        this.pageSize = treeCapacity * 4 + 1;
        this.treeCapacity = treeCapacity;
        this.deletedRecords = new ArrayList<>();
        this.deletedPages = new ArrayList<>();
        //this.tree = new ArrayList<>();
        this.path = new ArrayList<>();
        this.pathCopy = new ArrayList<>();
        Node root = new Node(treeCapacity);
        this.root = root;
        current = root;
        root.setNumber(pageNumber);
        pageNumber++;
        disc = new DiscIO("page.txt", treeCapacity);
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

    public List<Integer> getDeletedPages() {
        return deletedPages;
    }

    public void insert(int key, int[] record) throws IOException {
        current = root;
        path.add(root);
        if(search(key, root) != null) {
            path.clear();
            pathCopy.clear();
            return;
        }
        if(current.getValues().size() < 2 * treeCapacity){
            pathCopy.add(current);
            Element newElement = new Element(key, offset);
            current.getValues().add(newElement);
            current.getValues().sort(Comparator.comparingInt(Element::getKey));
        }
        else {
            if(!compensate(key, offset))
                split(key, offset);
        }
        disc.saveRecord(offset, key, record);
        offset+= Integer.BYTES * 7;
        savePage();
        path.clear();
        pathCopy.clear();
        root.getChildren().clear();
        //current = root;
    }

    public void delete(int key) throws IOException {
        current = root;
        path.add(root);
        Element temp = search(key, root);
        if(temp == null) {
            path.clear();
            pathCopy.clear();
            return;
        }
        if (current.getPointers().isEmpty()) { //leaf
            if(current.getValues().size() > treeCapacity)
                return; //simple delete
            else {
                if(!compensateDel(temp))
                    split(key, offset);
            }
        }
    }

    private void merge(Element delete) throws IOException {
        Node parent = path.get(path.indexOf(current) - 1);
        List<Node> children = parent.getChildren();
        if(children.size() < 2){ //Last child if d = 1
            parent.getChildren().clear();
            parent.getPointers().clear();
            pathCopy.add(parent);
            current = parent;
            return;
        }
        int index = parent.getChildren().indexOf(current);
        if(index < 1) { //merge with right
            Node siblingRight = disc.read(parent.getPointers().get(1));//children.get(1);
            children.add(1, siblingRight);
            children.remove(2);

            //current.getValues().remove(delete);
            current.getValues().add(parent.getValues().get(index));
            current.getValues().addAll(siblingRight.getValues());
            current.getPointers().addAll(siblingRight.getPointers());

            siblingRight.getValues().clear();
            parent.getPointers().remove(1);
            parent.getChildren().remove(1);

            pathCopy.add(siblingRight); pathCopy.add(current); pathCopy.add(parent);//nodes to resave

        }
        else {
            Node siblingLeft = disc.read(parent.getPointers().get(index - 1));
            //Node siblingRight = disc.read(parent.getPointers().get(1));//children.get(1);
            children.add(index - 1, siblingLeft);
            children.remove(index);

            current.getValues().addFirst(parent.getValues().get(index));
            current.getValues().addAll(0, siblingLeft.getValues());
            current.getPointers().addAll(siblingLeft.getPointers());

            siblingLeft.getValues().clear();

            pathCopy.add(siblingLeft); pathCopy.add(current); pathCopy.add(parent);


            Node siblingRight;
            if(parent.getPointers().size() - 1 > index) {
                siblingRight = disc.read(parent.getPointers().get(index + 1));//children.get(index + 1); //right sibling should exist
                children.add(index + 1, siblingRight);
                children.remove(index + 2);
            }
            else
                //return false; //No siblings to match

            //if(siblingRight.getValues().size() == treeCapacity * 2)
                //return false; //sibling is full

            //compensate with right
            //Node siblingRight = children.get(1);
            //Element newOne = new Element(key, offset);
            current.getValues().remove(delete);
            List<Element> temp = current.getValues();
            temp.add(parent.getValues().get(index));
            temp.addAll(siblingRight.getValues());
            //temp.add(newOne); //create temp list with all
            //temp.sort(Comparator.comparingInt(Element::getKey));
            int middleValue = (int) Math.round(temp.size()/2.0 - 1);
            Element middle = temp.get(middleValue); //choose middle

            parent.getValues().set(index, middle); // set parent to middle

            current.setValues(new ArrayList<>(temp.subList(0, middleValue)));

            siblingRight.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));

            if(!current.getPointers().isEmpty()){
                current.getPointers().add(siblingRight.getPointers().getFirst());
                current.getChildren().add(siblingRight.getChildren().getFirst());
                siblingRight.getPointers().removeFirst();
                siblingRight.getChildren().removeFirst();
            }

            pathCopy.add(siblingRight); pathCopy.add(current); pathCopy.add(parent);//nodes to resave
            //return true;
        }


    }

    public boolean updateRecord(int key, int[] data) throws IOException {
        Element temp = search(key, root);
        if(temp ==null)
            return false;
        disc.saveRecord(temp.getOffset(), key, data);
        return true;
    }

    private Node loadPage(long pageNum) throws IOException {
        return disc.read(pageNum);
    }

    private void fillNode(Node node) {
        for(int i = 0; i < treeCapacity * 2 + 1; i++)
            node.getChildren().add(null);
    }

    private void savePage() throws IOException {
        for (Node node : pathCopy)
            disc.save(node, node.getNumber());

    }

    private int find(int key, Node node){
        List<Element> temp = node.getValues();
        int i;
        for(i = 0; i < temp.size(); i++){
            if(temp.get(i).getKey() >= key)
                return i;
        }
        return i;
    }

    public Element search(int key, Node s) throws IOException {
        // Start from the root node

        // Base case: If the node is empty, return null
        if (s.getValues().isEmpty()) {
            return null;
        }

        // Find the index of the key in the current node
        int x = find(key, s);

        // If the key is found in the current node, return it
        if(x < (s.getValues().size())) {
            if (s.getValues().get(x).getKey() == key) {
                //current = null;  // Reset current node, if necessary
                return s.getValues().get(x);
            }
        }

        // If the key is not found and we have children, we need to search the correct child node
        if (!s.getPointers().isEmpty()) {
            // Add current node to path
            fillNode(s);
            /*if(x >= s.getPointers().size()) {
                current = null; //Value dont exist
                return null;
            }*/
            Node childNode = disc.read(s.getPointers().get(x));//s.getChildren().get(x);
            s.getChildren().add(x, childNode); s.getChildren().remove(x + 1);
            path.add(childNode);
            current = childNode;// Get the child node based on the index
            return search(key, childNode); // Recursively search in the child node
        }

        // If we reached a leaf node and the key is not found, return null
        return null;
    }

    private boolean compensateDel(Element delete) throws IOException {
        if(current == root)
            return false;
        //
        Node parent = path.get(path.indexOf(current) - 1);
        List<Node> children = parent.getChildren();
        if (children.size() < 2) //No siblings to compensate
            return false;
        int index = children.indexOf(current);
        if(index < 1) { //Right sibling should exist
            Node siblingRight = disc.read(parent.getPointers().get(1));//children.get(1);
            children.add(1, siblingRight);
            children.remove(2);
            if(siblingRight.getValues().size() == treeCapacity )
                return false; //sibling has exactly 'd' keys

            current.getValues().remove(delete);
            List<Element> temp = current.getValues();
            temp.add(parent.getValues().get(index));
            temp.addAll(siblingRight.getValues());
            //temp.add(newOne); //create temp list with all
            //temp.sort(Comparator.comparingInt(Element::getKey));
            int middleValue = (int) Math.round(temp.size()/2.0 - 1);
            Element middle = temp.get(middleValue); //choose middle

            parent.getValues().set(index, middle); // set parent to middle
            //redistribute equally

            current.setValues(new ArrayList<>(temp.subList(0, middleValue)));

            siblingRight.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));

            if(!current.getPointers().isEmpty()){
                current.getPointers().add(siblingRight.getPointers().getFirst());
                current.getChildren().add(siblingRight.getChildren().getFirst());
                siblingRight.getPointers().removeFirst();
                siblingRight.getChildren().removeFirst();
            }

            pathCopy.add(siblingRight); pathCopy.add(current); pathCopy.add(parent);//nodes to resave
            return true;
        }
        else {
            Node siblingLeft = disc.read(parent.getPointers().get(index - 1));
            //Node siblingRight = disc.read(parent.getPointers().get(1));//children.get(1);
            children.add(index - 1, siblingLeft);
            children.remove(index);
            if(!(siblingLeft.getValues().size() == treeCapacity)) {
                //sibling has space
                //Element newOne = new Element(key, offset);
                List<Element> temp = siblingLeft.getValues();
                temp.add(parent.getValues().get(index - 1));
                current.getValues().remove(delete);
                temp.addAll(current.getValues());
                //temp.add(newOne); //create temp list with all
                //temp.sort(Comparator.comparingInt(Element::getKey));
                int middleValue = (int) Math.round(temp.size()/2.0 - 1);
                Element middle = temp.get(middleValue); //choose middle

                parent.getValues().set(index - 1, middle); // set parent to middle

                siblingLeft.setValues(new ArrayList<>(temp.subList(0, middleValue)));

                current.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));

                if(!current.getPointers().isEmpty()){
                    current.getPointers().addFirst(siblingLeft.getPointers().getLast());
                    current.getChildren().addFirst(siblingLeft.getChildren().getLast());
                    siblingLeft.getPointers().removeLast();
                    siblingLeft.getChildren().removeLast();
                }

                pathCopy.add(siblingLeft); pathCopy.add(current); pathCopy.add(parent);//nodes to resave
                return true;
            }

            Node siblingRight;
            if(parent.getPointers().size() - 1 > index) {
                siblingRight = disc.read(parent.getPointers().get(index + 1));//children.get(index + 1); //right sibling should exist
                children.add(index + 1, siblingRight);
                children.remove(index + 2);
            }
            else
                return false; //No siblings to match

            if(siblingRight.getValues().size() == treeCapacity * 2)
                return false; //sibling is full

            //compensate with right
            //Node siblingRight = children.get(1);
            //Element newOne = new Element(key, offset);
            current.getValues().remove(delete);
            List<Element> temp = current.getValues();
            temp.add(parent.getValues().get(index));
            temp.addAll(siblingRight.getValues());
            //temp.add(newOne); //create temp list with all
            //temp.sort(Comparator.comparingInt(Element::getKey));
            int middleValue = (int) Math.round(temp.size()/2.0 - 1);
            Element middle = temp.get(middleValue); //choose middle

            parent.getValues().set(index, middle); // set parent to middle

            current.setValues(new ArrayList<>(temp.subList(0, middleValue)));

            siblingRight.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));

            if(!current.getPointers().isEmpty()){
                current.getPointers().add(siblingRight.getPointers().getFirst());
                current.getChildren().add(siblingRight.getChildren().getFirst());
                siblingRight.getPointers().removeFirst();
                siblingRight.getChildren().removeFirst();
            }

            pathCopy.add(siblingRight); pathCopy.add(current); pathCopy.add(parent);//nodes to resave
            return true;
        }

    }

    private boolean compensate(int key, long offset) throws IOException {
        if(current == root)
            return false;
        //Only possible in leaf
        Node parent = path.get(path.indexOf(current) - 1);
        List<Node> children = parent.getChildren();
        if (children.size() < 2)
            return false; //No siblings to compensate

        int index = children.indexOf(current);
        if(index < 1){ //Right sibling should exist
            Node siblingRight = disc.read(parent.getPointers().get(1));//children.get(1);
            children.add(1, siblingRight);
            children.remove(2);
            if(siblingRight.getValues().size() == treeCapacity * 2)
                return false; //sibling is full

            Element newOne = new Element(key, offset);
            List<Element> temp = siblingRight.getValues();
            temp.add(parent.getValues().get(index));
            temp.addAll(children.get(index).getValues());
            temp.add(newOne); //create temp list with all
            temp.sort(Comparator.comparingInt(Element::getKey));
            int middleValue = (int) Math.round(temp.size()/2.0 - 1);
            Element middle = temp.get(middleValue); //choose middle

            parent.getValues().set(index, middle); // set parent to middle
            //redistribute equally

            current.setValues(new ArrayList<>(temp.subList(0, middleValue)));

            siblingRight.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));
            pathCopy.add(siblingRight); pathCopy.add(current); pathCopy.add(parent);//nodes to resave
            return true;
        }
        else {
            Node siblingLeft = disc.read(parent.getPointers().get(index - 1));
            //Node siblingRight = disc.read(parent.getPointers().get(1));//children.get(1);
            children.add(index - 1, siblingLeft);
            children.remove(index);
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

                siblingLeft.setValues(new ArrayList<>(temp.subList(0, middleValue)));

                current.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));
                pathCopy.add(siblingLeft); pathCopy.add(current); pathCopy.add(parent);//nodes to resave
                return true;
            }

            Node siblingRight;
            if(parent.getPointers().size() - 1 > index) {
                siblingRight = disc.read(parent.getPointers().get(index + 1));//children.get(index + 1); //right sibling should exist
                children.add(index + 1, siblingRight);
                children.remove(index + 2);
            }
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

            parent.getValues().set(index, middle); // set parent to middle

            current.setValues(new ArrayList<>(temp.subList(0, middleValue)));

            siblingRight.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));
            pathCopy.add(siblingRight); pathCopy.add(current); pathCopy.add(parent);//nodes to resave
            return true;
        }
    }

    private void split(int key, long offset) {
        if(path.size() > 1) {
            Node parent = path.get(path.indexOf(current) - 1);
            List<Node> children = parent.getChildren();
            Node newNode = new Node(treeCapacity);
            newNode.setNumber(pageNumber++);
            if(!pathCopy.contains(current))
                pathCopy.add(current);
            pathCopy.add(newNode); pathCopy.add(parent); //all nodes to resave
            if (current.getPointers().isEmpty()) { //Code below to add to leaf
                Element newOne = new Element(key, offset);

                current.getValues().add(newOne);
                current.getValues().sort(Comparator.comparingInt(Element::getKey));

                int middleValue = (int) Math.round(current.getValues().size() / 2.0 - 1);
                List<Element> tempList; //redistribute equally

                List<Element> temp = current.getValues();
                tempList = new ArrayList<>(temp.subList(middleValue + 1, temp.size()));
                newNode.setValues(tempList);
                List<Element> tempList1;
                tempList1 = new ArrayList<>(temp.subList(0, middleValue));
                Element middle = temp.get(middleValue);
                temp.clear();
                temp.addAll(tempList1);

                int index = children.indexOf(current);
                parent.getChildren().add(index + 1, newNode);
                parent.getPointers().add(index + 1, newNode.getNumber());
                parent.getValues().add(index, middle);
            } else if (current.getValues().size() > treeCapacity * 2) { //Works on root and other
                int middleValue = (int) Math.round(current.getValues().size() / 2.0 - 1);
                List<Element> tempList; //redistribute equally

                List<Element> temp = current.getValues();
                tempList = new ArrayList<>(temp.subList(middleValue + 1, temp.size()));
                newNode.setValues(tempList);
                List<Element> tempList1;
                tempList1 = new ArrayList<>(temp.subList(0, middleValue));
                Element middle = temp.get(middleValue);
                temp.clear();
                temp.addAll(tempList1);

                int index = children.indexOf(current);
                parent.getChildren().add(index + 1, newNode);
                parent.getPointers().add(index + 1, newNode.getNumber());
                parent.getValues().add(index, middle);

                newNode.setPointers(new ArrayList<>(current.getPointers().subList(middleValue + 1, current.getPointers().size())));
                newNode.setChildren(new ArrayList<>(current.getChildren().subList(middleValue + 1, current.getChildren().size())));

                current.setPointers(new ArrayList<>(current.getPointers().subList(0, middleValue + 1)));
                current.setChildren(new ArrayList<>(current.getChildren().subList(0, middleValue + 1)));
            }
            path.remove(current);
            current = parent;
            split(key, offset);
        }
        else {
            if(current.getValues().size() > treeCapacity * 2 || current.getChildren().isEmpty()){ //root too full or just created
                Node temp = root;
                Node newRoot = new Node(treeCapacity);
                newRoot.setNumber(0);
                root.setNumber(pageNumber++);
                root = newRoot;
                newRoot.getChildren().add(temp);
                newRoot.getPointers().add(current.getNumber());
                path.addFirst(root);
                split(key, offset);
            }
        }
    }

    public void printTree(Node node, int level) throws IOException {
        if (node == null) return;

        // Print current level and its values
        System.out.println("Level " + level + ": " + node.toString());

        // Recursively print the children if they exist
        for (long child : node.getPointers()) {
            printTree(disc.read(child), level + 1);
        }
    }

    // To print the whole tree starting from root
    public void display() throws IOException {
        printTree(root, 0);
        disc.showResults();
    }

    public void deleteFile() {
        disc.deleteFile();
    }
}
