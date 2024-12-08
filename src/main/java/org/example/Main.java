package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main() throws IOException {
        BTree temp = new BTree(2);
        DiscIO disc = new DiscIO("Records.txt", 2);
        int[] array = new int[]{7, 7, 7, 7, 7, 7};
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
        temp.printInOrder();
        //temp.deleteFile();

    }
}
