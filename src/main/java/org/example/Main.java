package org.example;

import java.io.IOException;

public class Main {
    public static void main() throws IOException {
        BTree temp = new BTree(1);
        DiscIO disc = new DiscIO("Records.txt", 1);
        int[] array = new int[]{7, 7, 7, 7, 7, 7};/* temp.insert(0, array); temp.insert(6, array);
        Element test = temp.search(6, temp.getRoot()); Record my = disc.readRecord(test.getOffset());
        System.out.println(my.toString());
        for (int i = 4; i < 74; i+=4) {
            array[3]=i;
            temp.insert(i, array);
        }
        for (int i = 29; i < 40; i+=2) {
            array[2]=i;
            temp.insert(i, array);
        }
        for (int i = 0; i < 31; i++) {
            array[4]=i;
            temp.insert(i, array);
        }
        Element t = temp.search(2, temp.getRoot());
        temp.display();

        test = temp.search(26, temp.getRoot()); my = disc.readRecord(test.getOffset());
        System.out.println(my.toString());*/
        for (int i = 4; i < 74; i+=4) {
            array[3]=i;
            temp.insert(i, array);
        }
        for (int i = 29; i < 40; i+=2) {
            array[2]=i;
            temp.insert(i, array);
        }
        for (int i = 0; i < 31; i++) {
            array[4]=i;
            temp.insert(i, array);
        }
        temp.display();
        for (int i = 4; i < 65; i+=4) {
            temp.delete(i);
        }
        /*temp.delete(8);
        temp.delete(5);
        temp.delete(3);*/
        temp.display();
        temp.deleteFile();
        //System.out.print(Math.round(5/2.0));
    }
}
