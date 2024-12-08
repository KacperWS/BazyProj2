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
        this.path = new ArrayList<>();
        this.pathCopy = new ArrayList<>();
        Node root = new Node(treeCapacity);
        this.root = root;
        current = root;
        root.setNumber(pageNumber);
        pageNumber++;
        disc = new DiscIO("page.txt", treeCapacity);
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
        if(search(key, root, false) != null) {
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
        disc.showOp();
    }

    public void delete(int key) throws IOException {
        current = root;
        path.add(root);
        Element temp = search(key, root, false);
        if(temp == null) {
            path.clear();
            pathCopy.clear();
            return;
        }
        if (current.getPointers().isEmpty()) { //leaf
            if(current.getValues().size() > treeCapacity || current == root) {
                current.getValues().remove(temp);
                pathCopy.add(current);
                //disc.saveRecord(temp.getOffset(), -1, new int[]{-1, -1, -1, -1, -1, -1});
            }
            else {
                current.getValues().remove(temp);
                do {
                    if(current == root && root.getValues().isEmpty()) {
                        Node newRoot = root.getChildren().getFirst();
                        newRoot.setNumber(0);
                        root = newRoot;
                        break;
                    } else if (current == root) {
                        break;
                    }
                    if (!compensateDel()) {
                        merge();
                    }
                }while (current.getValues().size() < treeCapacity);
                //disc.saveRecord(temp.getOffset(), -1, new int[]{-1, -1, -1, -1, -1, -1});
            }
        }
        else { //Not leaf
            Node tempNode = current;
            Element replace = search(key - 1, current, true);
            tempNode.getValues().add(tempNode.getValues().indexOf(temp), replace); //Replacing key
            tempNode.getValues().remove(temp);
            current.getValues().remove(replace);
            pathCopy.add(current); pathCopy.add(tempNode);
            if(current.getValues().size() < treeCapacity){
                do {
                    if(current == root && root.getValues().isEmpty()) {
                        Node newRoot = root.getChildren().getFirst();
                        newRoot.setNumber(0);
                        root = newRoot;
                        break;
                    } else if (current == root) {
                        break;
                    }
                    if (!compensateDel()) {
                        merge();
                    }
                }while (current.getValues().size() < treeCapacity);
                //disc.saveRecord(temp.getOffset(), -1, new int[]{-1, -1, -1, -1, -1, -1});
            }
        }
        disc.saveRecord(temp.getOffset(), -1, new int[]{-1, -1, -1, -1, -1, -1});
        savePage();
        path.clear();
        pathCopy.clear();
        root.getChildren().clear();
        disc.showOp();
    }

    private void merge() throws IOException {
        Node parent = path.get(path.indexOf(current) - 1);
        List<Node> children = parent.getChildren();
        /*if(parent.getPointers().size() < 2){ //Last child if d = 1
            parent.getChildren().clear();
            parent.getPointers().clear();
            pathCopy.add(parent);
            current = parent;
            return;
        }*/
        int index = parent.getChildren().indexOf(current);
        if(index < 1) { //merge with right
            Node siblingRight = loadPage(parent, parent.getPointers().get(1), 1);
            current.getValues().add(parent.getValues().get(index));
            current.getValues().addAll(siblingRight.getValues());
            current.getPointers().addAll(siblingRight.getPointers());

            siblingRight.getValues().clear();
            parent.getValues().removeFirst();
            parent.getPointers().remove(1);
            parent.getChildren().remove(1);

            pathCopy.add(siblingRight); pathCopy.add(parent);//nodes to resave
        }
        else {
            Node siblingLeft = loadPage(parent, parent.getPointers().get(index - 1), index - 1);
            children.add(index - 1, siblingLeft);
            children.remove(index);

            current.getValues().addFirst(parent.getValues().get(index - 1));
            current.getValues().addAll(0, siblingLeft.getValues());
            current.getPointers().addAll(0, siblingLeft.getPointers());

            siblingLeft.getValues().clear();
            parent.getValues().remove(index - 1);
            parent.getPointers().remove(index - 1);
            parent.getChildren().remove(index - 1);

            pathCopy.add(siblingLeft); pathCopy.add(parent);

            /*Node siblingRight;
            if(parent.getPointers().size() - 1 > index) {
                siblingRight = disc.read(parent.getPointers().get(index + 1));//children.get(index + 1); //right sibling should exist
                children.add(index + 1, siblingRight);
                children.remove(index + 2);
            }
            else
                return; //No siblings to match*/
        }
        if(!pathCopy.contains(current))
            pathCopy.add(current);
        path.remove(current);
        current = parent;
    }

    public boolean updateRecord(int key, int[] data) throws IOException {
        Element temp = search(key, root, false);
        if(temp ==null)
            return false;
        disc.saveRecord(temp.getOffset(), key, data);
        return true;
    }

    private Node loadPage(Node s, long pageNum, int index) throws IOException {
        Node child = s.getChildren().get(s.getPointers().indexOf(pageNum));
        if(child == null) {
            Node childNode = disc.read(pageNum);
            s.getChildren().add(index, childNode);
            s.getChildren().remove(index + 1);
            return childNode;
        }
        else
            return child;
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

    private Element search(int key, Node s, boolean dfs) throws IOException {

        if (s.getValues().isEmpty()) {
            return null;
        }

        int x = find(key, s);

        if(x < (s.getValues().size())) {
            if (s.getValues().get(x).getKey() == key) {
                return s.getValues().get(x);
            }
        }

        if (!s.getPointers().isEmpty()) {
            fillNode(s);
            Node childNode = loadPage(s, s.getPointers().get(x), x);
            path.add(childNode);
            current = childNode;
            return search(key, childNode, dfs);
        }

        if(dfs)
            return s.getValues().getLast();
        else
            return null;
    }

    public Element search2 (int key) throws IOException {
        current = root;
        Element temp = search(key, root, false);
        disc.showOp();
        return temp;
    }

    private boolean compensateDel() throws IOException {
        if(current == root)
            return false;
        //
        Node parent = path.get(path.indexOf(current) - 1);
        List<Node> children = parent.getChildren();
        if (parent.getPointers().size() < 2) //No siblings to compensate
            return false;
        int index = children.indexOf(current);
        if(index < 1) { //Right sibling should exist
            Node siblingRight = loadPage(parent, parent.getPointers().get(1), 1);
            if(siblingRight.getValues().size() == treeCapacity )
                return false; //sibling has exactly 'd' keys

            List<Element> temp = current.getValues();
            temp.add(parent.getValues().get(index));
            temp.addAll(siblingRight.getValues());
            int middleValue = (int) Math.round(temp.size()/2.0 - 1);
            Element middle = temp.get(middleValue); //choose middle

            parent.getValues().set(index, middle); // set parent to middle
            //redistribute equally

            current.setValues(new ArrayList<>(temp.subList(0, middleValue)));

            siblingRight.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));

            if(!current.getPointers().isEmpty()){
                while(current.getPointers().size() <= current.getValues().size()) {
                    current.getPointers().add(siblingRight.getPointers().getFirst());
                    //current.getChildren().add(siblingRight.getChildren().getFirst());
                    siblingRight.getPointers().removeFirst();
                    //siblingRight.getChildren().removeFirst();
                }
            }

            pathCopy.add(siblingRight); pathCopy.add(current); pathCopy.add(parent);//nodes to resave
            return true;
        }
        else {
            Node siblingLeft = loadPage(parent, parent.getPointers().get(index - 1), index - 1);
            if(!(siblingLeft.getValues().size() == treeCapacity)) {
                //sibling has space
                List<Element> temp = siblingLeft.getValues();
                temp.add(parent.getValues().get(index - 1));
                temp.addAll(current.getValues());
                int middleValue = (int) Math.round(temp.size()/2.0 - 1);
                Element middle = temp.get(middleValue); //choose middle

                parent.getValues().set(index - 1, middle); // set parent to middle

                siblingLeft.setValues(new ArrayList<>(temp.subList(0, middleValue)));

                current.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));

                if(!current.getPointers().isEmpty()){
                    while(current.getPointers().size() <= current.getValues().size()) {
                        current.getPointers().addFirst(siblingLeft.getPointers().getLast());
                        //current.getChildren().addFirst(siblingLeft.getChildren().getLast());
                        siblingLeft.getPointers().removeLast();
                        //siblingLeft.getChildren().removeLast();
                    }
                }

                pathCopy.add(siblingLeft); pathCopy.add(current); pathCopy.add(parent);//nodes to resave
                return true;
            }

            Node siblingRight;
            if(parent.getPointers().size() - 1 > index) {
                siblingRight = loadPage(parent, parent.getPointers().get(index + 1), index + 1);
            }
            else
                return false; //No siblings to match

            if(siblingRight.getValues().size() == treeCapacity)
                return false; //sibling is full

            //compensate with right
            List<Element> temp = current.getValues();
            temp.add(parent.getValues().get(index));
            temp.addAll(siblingRight.getValues());
            int middleValue = (int) Math.round(temp.size()/2.0 - 1);
            Element middle = temp.get(middleValue); //choose middle

            parent.getValues().set(index, middle); // set parent to middle

            current.setValues(new ArrayList<>(temp.subList(0, middleValue)));

            siblingRight.setValues(new ArrayList<>(temp.subList(middleValue + 1, temp.size())));

            if(!current.getPointers().isEmpty()){
                while(current.getPointers().size() <= current.getValues().size()) {
                    current.getPointers().add(siblingRight.getPointers().getFirst());
                    //current.getChildren().add(siblingRight.getChildren().getFirst());
                    siblingRight.getPointers().removeFirst();
                    //siblingRight.getChildren().removeFirst();
                }
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
        if (parent.getPointers().size() < 2)
            return false; //No siblings to compensate

        int index = children.indexOf(current);
        if(index < 1){ //Right sibling should exist
            Node siblingRight = loadPage(parent, parent.getPointers().get(1), 1);
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
            Node siblingLeft = loadPage(parent, parent.getPointers().get(index - 1), index - 1);
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
                siblingRight = loadPage(parent, parent.getPointers().get(index + 1), index + 1);
            }
            else
                return false; //No siblings to match

            if(siblingRight.getValues().size() == treeCapacity * 2)
                return false; //sibling is full

            //compensate with right
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

        System.out.print("Level " + level + ": ");
        for(Element key : node.getValues())
            System.out.print(key.getKey() + " ");
        System.out.println();
        for (long child : node.getPointers()) {
            printTree(disc.read(child), level + 1);
        }
    }

    public void printInOrder() throws IOException {
        System.out.println("Keys ascending: ");
        printInOrder(root);
    }

    // Helper method for in-order traversal
    private void printInOrder(Node node) throws IOException {
        int i = 0;
        while (i < node.getValues().size()) {
            // Traverse the left child (if any)
            if (!node.getPointers().isEmpty()) {
                printInOrder(disc.read(node.getPointers().get(i)));
            }

            // Print the current key
            System.out.print(node.getValues().get(i).getKey() + " ");

            // Move to the next key
            i++;
        }

        // If the node is not a leaf, traverse the right child
        if (!node.getPointers().isEmpty()) {
            printInOrder(disc.read(node.getPointers().get(i)));
        }
    }

    public void display() throws IOException {
        disc.invertCounters();
        System.out.println("Tree: ");
        printTree(root, 0);
        disc.invertCounters();
        disc.showResults();
    }

    public void deleteFile() {
        disc.deleteFile();
    }
}
