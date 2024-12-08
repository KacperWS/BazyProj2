package org.example;

import java.io.*;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.nio.ByteBuffer;

public class DiscIO {
    private String filename;
    private String filenameNoext;
    private String filenameRecords = "records.txt";
    private int readCounter;
    private int writeCounter;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    private RandomAccessFile raf;
    private final int recordSize = 7;
    private final int pageSize;
    private final int pageSizeBytes;
    private int records;
    private int recToGenerate = 10000 * recordSize;
    private boolean show = true;

    public DiscIO(String filename, int pageSize) {
        this.filename = filename;
        this.pageSize = pageSize;
        this.pageSizeBytes = pageSize * 2 * Integer.BYTES + pageSize * 2 * Long.BYTES + (pageSize * 2 + 1) * Long.BYTES;
        String[] stringArray = filename.split("\\.");
        this.filenameNoext = stringArray[0];
        this.records = 0;
    }

    public void setRecToGenerate(int recToGenerate) {
        this.recToGenerate = recToGenerate * recordSize;
    }

    public int getRecToGenerate() {
        return recToGenerate;
    }

    public String getFilename() {
        return filenameNoext;
    }

    public void setFilename(String filename){
        this.filename = filename;
        String[] stringArray = filename.split("\\.");
        this.filenameNoext = stringArray[0];
    }

    public void closeIN() throws IOException {
        if (bis != null) {
            bis.close();
        }
    }

    public void closeOUT() throws IOException {
        if (bos != null) {
            bos.close();
        }
    }

    public void closeRAF() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

    public void closeALL() throws IOException {
        closeIN();
        closeOUT();
        closeRAF();
    }

    public void openIN() throws IOException {
        this.bis = new BufferedInputStream(new FileInputStream(filename));
    }

    public void openOUT(String Filename, boolean mode) throws IOException {
        this.bos = new BufferedOutputStream(new FileOutputStream(Filename, mode));
    }

    public void openRAF(String op) throws FileNotFoundException {
        this.raf = new RandomAccessFile(filename, op);
    }

    public void openRAF2(String filename, String op) throws FileNotFoundException {
        this.raf = new RandomAccessFile(filename, op);
    }

    public void createDataset() {
        String fileName = "ter.txt";
        deleteFile();
        byte[] binaryData = new byte[4 * this.recToGenerate];
        Random rand = new Random();

        ByteBuffer test = ByteBuffer.allocate(4 * this.recToGenerate);
        for (int i = 0; i < binaryData.length / 4; i++) {
            rand.nextInt();
            int value = rand.nextInt(8);

            test.putInt(value);
        }
        test.flip(); test.get(binaryData);

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName))) {
            int chunkSize = 1024;
            for (int i = 0; i < binaryData.length; i += chunkSize) {
                int bytesToWrite = Math.min(chunkSize, binaryData.length - i);
                bos.write(binaryData, i, bytesToWrite);
            }

            System.out.println("Binary data saved successfully in chunks to " + (4 * this.recToGenerate));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void invertCounters() {
        this.show = !show;
    }

    public Record readRecord(long offset) throws IOException{
        openRAF2("Records.txt","r");
        byte[] buffer = new byte[Integer.BYTES * recordSize];
        raf.seek(offset);
        Record newRec;
        if (raf.read(buffer) != -1) {
            ByteBuffer bufferme;
            bufferme = ByteBuffer.wrap(buffer);
            long id = bufferme.getInt();
            int i = 0; int hmm = this.recordSize - 1;
            int[] array = new int [hmm];
            while(i < recordSize - 1){
                array[i%hmm] = bufferme.getInt();
                i++;
            }
            newRec = new Record(id, array);
        }else {
            closeRAF(); //File ended
            return null; //Indicates that file ended
        }
        //writeCounter++;
        closeRAF();
        return newRec;
    }

    public void saveRecord(long offset, int key, int[] data) throws IOException{
        openRAF2("Records.txt", "rw");
        byte[] buffer = new byte[Integer.BYTES * recordSize];
        raf.seek(offset);
        ByteBuffer temp = ByteBuffer.allocate(Integer.BYTES * recordSize);
        temp.putInt(key);
        for(int i = 0; i < 6; i++)
            temp.putInt(data[i]);

        temp.flip();
        temp.get(buffer);
        raf.write(buffer);
        //readCounter++;
        closeRAF();
    }

    public Node read(long pageNumber) throws IOException{
        openRAF("r");
        byte[] buffer = new byte[pageSizeBytes]; //2d keys + 2d keys offsets and 2d+1 offsets
        //List<Record> lista = new ArrayList<>();
        Node temp = new Node(pageSize);
        raf.seek(pageNumber * pageSizeBytes);
        if (raf.read(buffer) != -1) {
            ByteBuffer bufferme;
            bufferme = ByteBuffer.wrap(buffer);
            IntBuffer buffer1 = bufferme.asIntBuffer();
            LongBuffer buffer2 = bufferme.asLongBuffer();
            for (int i = 0; i < pageSize * 2; i++) {
                if(buffer1.get(i) != -1)
                    temp.getValues().add(new Element(buffer1.get(i), buffer2.get(i + pageSize)));
            }
            for (int i = 0, j = pageSize * 3; i < pageSize * 2 + 1; i++ , j++) {
                if(buffer2.get(j) != -1)
                    temp.getPointers().add(buffer2.get(j));
            }
        }else {
            closeRAF(); //File ended
            return null; //Indicates that file ended
        }
        if(show)
            readCounter++;
        closeRAF();
        temp.setNumber(pageNumber);
        return temp;
    }

    public void save(Node node, long pageNumber) throws IOException{
        //openOUT(addon, true);
        openRAF("rw");
        raf.seek(pageNumber * pageSizeBytes);
        byte[] binaryData = new byte[pageSizeBytes];
        ByteBuffer temp = ByteBuffer.allocate(pageSizeBytes);
        //Puts elements to buffer /key /offset
        for(int i = 0; i < pageSize * 2; i++) {
            if(i < node.getValues().size())
                temp.putInt(node.getValues().get(i).getKey());
            else
                temp.putInt(-1);
        }
        for(int i = 0; i < pageSize * 2; i++) {
            if(i < node.getValues().size())
                temp.putLong(node.getValues().get(i).getOffset());
            else
                temp.putLong(-1);
        }

        for (int i = 0; i < pageSize * 2 + 1; i++) {//Pointers to childs
            if(i < node.getPointers().size())
                temp.putLong(node.getPointers().get(i));
            else
                temp.putLong(-1);
        }

        temp.flip();
        temp.get(binaryData);
        raf.write(binaryData);
        if(show)
            writeCounter++;
        closeRAF();
    }


    public void deleteFile(){
        Path path = Paths.get(filename);
        try {
            Files.delete(path);
        } catch (IOException e) {
            System.out.println("Failed to delete the file. Error: " + e.getMessage());
        }
    }

    public void showFile(){
        try (FileInputStream fis = new FileInputStream(filename)) {
            byte[] byteBuffer = new byte[4];
            int i = 0;
            int[] temp = new int[6];
            while (fis.read(byteBuffer) != -1) {
                ByteBuffer byteBufferWrapper = ByteBuffer.wrap(byteBuffer);
                int number = byteBufferWrapper.getInt();
                if(i%6 == 0 && i > 0){
                    int x = temp[5];
                    int suma = 0, xpower = x;
                    suma += temp[0] + temp[1] * xpower;
                    suma+= temp[2] * (xpower *= x);
                    suma+= temp[3] * (xpower *= x);
                    suma+= temp[4] * (xpower * x);
                    System.out.println("Rekord = " + suma);
                }
                temp[i%6] = number;
                if(i%6 == 0)
                    System.out.print(i/6 + ". ");
                System.out.print(number + " ");
                i++;
            }
            int x = temp[5];
            int suma = 0, xpower = x;
            suma += temp[0] + temp[1] * xpower;
            suma+= temp[2] * (xpower *= x);
            suma+= temp[3] * (xpower *= x);
            suma+= temp[4] * (xpower * x);
            System.out.println("Rekord = " + suma);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showResults(){
        System.out.println("Reads: " + readCounter + " Writes: " + writeCounter);
    }

}

