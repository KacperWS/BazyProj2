package org.example;

public class Main {
    public static void main() {
        BTree temp = new BTree(1);
        for (int i = 0; i < 15; i++)
            temp.insert(i,1);
        Element t = temp.search(2, temp.getRoot());
        //System.out.print(Math.round(5/2.0));
    }
}
