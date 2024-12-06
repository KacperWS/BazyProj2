package org.example;

import java.io.IOException;

public class Main {
    public static void main() throws IOException {
        BTree temp = new BTree(1);
        for (int i = 4; i < 74; i+=4)
            temp.insert(i,i);
        for (int i = 29; i < 40; i+=2)
            temp.insert(i,i);
        for (int i = 0; i < 31; i++)
            temp.insert(i,i);
        Element t = temp.search(2, temp.getRoot());
        temp.display();
        temp.delete();
        //System.out.print(Math.round(5/2.0));
    }
}
