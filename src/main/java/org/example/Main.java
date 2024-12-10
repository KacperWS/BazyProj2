package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String args[]) throws IOException {
        Scanner scanner = new Scanner(System.in);
        int[] array = new int[]{7, 7, 7, 7, 7, 7};
        List<Integer> delete = new ArrayList<>();
        List<Integer> update = new ArrayList<>();
        RNG tester = new RNG(10000);
        BTree tree = null;

        System.out.println("What can I do for you? (B-Tree)");

        boolean loop = true;
        while(loop) {
            System.out.println("1. Generate set \n2. Create tree \n3. Read from keyboard \n4. Change options \n5. Exit");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1": {
                    System.out.println("Specify the set Insert Delete Update (Not all operation might be excutable):");
                    choice = scanner.nextLine();
                    String[] stringArray = choice.split(" ");

                    array[3] = 2; array[2] = 2 + 3 * 4;
                    for(int j = 0; j < Integer.parseInt(stringArray[0]); j++){
                        assert tree != null;
                        tree.insert((int) tester.random(), array);
                    }

                    array[1] = 2; array[3] = 2 + 3 * 4;
                    for(int j = 0; j < Integer.parseInt(stringArray[1]); j++){
                        assert tree != null;
                        tree.delete((int)tester.value());
                    }

                    for(int j = 0; j < Integer.parseInt(stringArray[2]); j++){
                        assert tree != null;
                        tree.updateRecord((int)tester.value(), array);
                    }

                    break;
                }

                case "2": {
                    System.out.println("Creating tree, give d value:");
                    choice = scanner.nextLine(); int temp = Integer.parseInt(choice);
                    tree = new BTree(temp);
                    break;
                }

                case "3": {
                    System.out.println("Choose operation:");
                    System.out.println("1. Insert");
                    System.out.println("2. Delete");
                    System.out.println("3. Update");
                    System.out.println("4. Search");
                    System.out.println("5. Exit mode");
                    boolean loop1 = true;
                    while(loop1) {
                        choice = scanner.nextLine();
                        switch (choice) {
                            case "1": {
                                choice = scanner.nextLine();
                                String[] stringArray = choice.split(" ");
                                int[] array1 = new int[6];
                                for(int i = 1; i < 7; i++)
                                    array1[i] = Integer.parseInt(stringArray[i]);
                                assert tree != null;
                                tree.insert(Integer.parseInt(stringArray[0]), array1);
                                break;
                            }

                            case "2": {
                                choice = scanner.nextLine();
                                int temp = Integer.parseInt(choice);
                                assert tree != null;
                                tree.delete(temp);
                                break;
                            }

                            case "3": {
                                choice = scanner.nextLine();
                                String[] stringArray = choice.split(" ");
                                int[] array1 = new int[6];
                                for(int i = 1; i < 7; i++)
                                    array1[i] = Integer.parseInt(stringArray[i]);
                                assert tree != null;
                                if(tree.updateRecord(Integer.parseInt(stringArray[0]), array1))
                                    System.out.println("Success");
                                else
                                    System.out.println("Not found");
                                break;
                            }

                            case "4": {
                                choice = scanner.nextLine();
                                int temp = Integer.parseInt(choice);
                                assert tree != null;
                                Element elem = tree.search2(temp);
                                System.out.println(elem);
                                break;
                            }

                            case "5": {
                                loop1 = false;
                                break;
                            }

                            default: {
                                System.out.println("This is not a correct option");
                                break;
                            }

                        }
                    }

                    break;
                }

                case "4": {
                    System.out.println("1. Tree 2. Show rec 3. Change seed 4.Quit");
                    //System.out.println("Show all: " + Boolean.toString(!variables[0]) + " Show nothing: " + Boolean.toString(!variables[1]));
                    choice = scanner.nextLine(); int ch = Integer.parseInt(choice);
                    if(ch==1) {
                        assert tree != null;
                        tree.display();
                    }
                    else if (ch==2) {
                        assert tree != null;
                        tree.show();
                    }else if(ch==2) {
                        choice = scanner.nextLine();
                        tester.changeSeed(Integer.parseInt(choice));
                    }

                    break;
                }

                case "5": {
                    loop = false;
                    break;
                }

                default: {
                    System.out.println("This is not a correct option");
                    break;
                }
            }
        }
        assert tree != null;
        tree.saveSettings();
        scanner.close();
    }
}
