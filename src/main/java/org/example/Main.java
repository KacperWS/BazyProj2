package org.example;

import java.io.IOException;

public class Main {
    public static void main() throws IOException {
        BTree temp = new BTree(1);
        for (int i = 4; i < 74; i+=4)
            temp.insert(i,i);
        for (int i = 29; i < 45; i++)
            temp.insert(i,i);/*
        for (int i = 5; i < 11; i+=2)
            temp.insert(i,i);*/
        Element t = temp.search(2, temp.getRoot());
        temp.display();
        temp.delete();
        //System.out.print(Math.round(5/2.0));
    }
}
