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
    private int read;
    private int write;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    private RandomAccessFile raf;
    private final int recordSize = 7;
    private final int pageSize;
    private final int pageSizeBytes;
    private int records;
    private int recToGenerate = 10000 * recordSize;
    private boolean show = true;

    public DiscIO(String filename, int pageSize) throws IOException {
        this.filename = filename;
        this.pageSize = pageSize;
        this.pageSizeBytes = pageSize * 2 * Integer.BYTES + pageSize * 2 * Long.BYTES + (pageSize * 2 + 1) * Long.BYTES;
        String[] stringArray = filename.split("\\.");
        this.filenameNoext = stringArray[0];
        this.records = 0;
        read = 0; write = 0;
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

    public void closeRAF() throws IOException {
        if (raf != null) {
            raf.close();
        }
    }

    public void openRAF(String op) throws FileNotFoundException {
        this.raf = new RandomAccessFile(filename, op);
    }

    public void openRAF2(String filename, String op) throws FileNotFoundException {
        this.raf = new RandomAccessFile(filename, op);
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
        if(show)
            readCounter++;
        read++;
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
        if(show)
            readCounter++;
        read++;
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
        read++;
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
        write++;
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
        String te = filename;
        filename = "Records.txt";
        try (FileInputStream fis = new FileInputStream(filename)) {
            byte[] byteBuffer = new byte[4];
            int i = 0;
            int[] temp = new int[7];
            while (fis.read(byteBuffer) != -1) {
                ByteBuffer byteBufferWrapper = ByteBuffer.wrap(byteBuffer);
                int number = byteBufferWrapper.getInt();
                temp[i%7] = number;
                if(i%7 == 0) {
                    if(i > 1)
                        System.out.println();
                    System.out.print(i / 7 + ". ");
                }
                System.out.print(number + " ");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
        filename = te;
    }
    
    public void saveSettings(long[] data, List<Integer> a, List<Long> b) throws IOException {
        filename = "Settings.txt";
        openRAF("rw");
        ByteBuffer temp = ByteBuffer.allocate(data.length * Long.BYTES + a.size() * Long.BYTES + b.size() * Long.BYTES + 2 * Long.BYTES);

        for(long value : data)
            temp.putLong(value);
        temp.putLong(a.size());
        temp.putLong(b.size());
        for(int value : a) //page
            temp.putLong(value);
        for(long value : b) //record
            temp.putLong(value);

        byte[] binaryData = new byte[data.length * Long.BYTES + a.size() * Long.BYTES + b.size() * Long.BYTES + 2 * Long.BYTES];
        temp.flip();
        temp.get(binaryData);
        raf.write(binaryData);
        closeRAF();
    }

    public long[] readSettings(BTree tree) throws IOException {
        String temp = filename;
        filename = "Settings.txt";
        openRAF("r");
        long[] data = new long[5];
        byte[] buffer = new byte[data.length * Long.BYTES];
        if (raf.read(buffer) != -1) {
            ByteBuffer bufferme;
            bufferme = ByteBuffer.wrap(buffer);
            LongBuffer as = bufferme.asLongBuffer();
            for (int i = 0; i < as.capacity(); i++)
                data[i] = as.get(i);
            if(data[2] == tree.getTreeCapacity()) {
                buffer = new byte[(int) (data[3] * Long.BYTES + data[4] * Long.BYTES)];
                raf.read(buffer);
                List<Integer> page = new ArrayList<>();
                List<Long> rec = new ArrayList<>();
                bufferme = ByteBuffer.wrap(buffer);
                LongBuffer buff = bufferme.asLongBuffer();
                int i;
                for (i = 0; i < data[3]; i++)
                    page.add((int) buff.get(i));

                for (; i < data[4] + data[3]; i++)
                    rec.add(buff.get(i));

                tree.loadSett(page, rec);
            }
        }
        else {
            filename = temp;
            closeRAF();
        }
        closeRAF();
        filename = temp;
        return data;
    }

    public void showOp() {
        System.out.println("Reads: " + read + " Writes: " + write);
        read =0; write = 0;
    }

    public void clear() {
        writeCounter = 0;
        readCounter = 0;
    }

    public void showResults(){
        System.out.println("Reads: " + readCounter + " Writes: " + writeCounter);
    }

}

