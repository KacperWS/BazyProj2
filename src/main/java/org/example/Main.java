package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main() throws IOException {
        BTree temp = new BTree(2);
        DiscIO disc = new DiscIO("Records.txt", 2);
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
        /*for (int i = 4; i < 74; i+=4) {
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
            temp.delete(i); //System.out.println(" AAA" +i);
            //temp.display();
        }
        temp.delete(8);
        temp.delete(5);
        temp.delete(3);*/
        List<Integer> delete = new ArrayList<>();
        RNG tester = new RNG(1000);
        for (int i = 0; i < 10; i++) {
            array[4]=i;
            long val = tester.random();
            if(i % 3 == 0)
                delete.add((int) val);
            temp.insert((int) val, array);
        }
        //temp.display();
        for (Integer integer : delete) {
            temp.delete(integer);
        }
        temp.display();
        /*Element test = temp.search(6, temp.getRoot(), false); Record my = disc.readRecord(test.getOffset());
        System.out.println(my.toString());
        temp.updateRecord(6, new int[]{0, 1, 2, 3, 3, 4});
        test = temp.search(6, temp.getRoot(), false); my = disc.readRecord(test.getOffset());
        System.out.println(my.toString());*/

        temp.deleteFile();
        //System.out.print(Math.round(5/2.0));

        /*RNG tester = new RNG(1000);
        for (int j = 0; j < 100000; j++)
            System.out.println(tester.random());
        System.out.println("Random numbers = " + tester.check());*/
    }
}
