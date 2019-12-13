/*
 * @projectName race2018
 * @package io.openmessaging
 * @className io.openmessaging.QueueStoreOne
 * @copyright Copyright 2019 Thuisoft, Inc. All rights reserved.
 */
package io.openmessaging;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


/**
 * QueueStoreOne
 * @description QueueStoreOne
 * @author liubolun
 * @date 2019年12月12日 13:47
 * @version 3.1.1
 */
public class QueueStoreOne extends QueueStore {

    final static String PATH = "/tmp/queue/";

    final static long HEADER_BROKER = 1000L;

    int HEADER_LENGTH = 10;

    long currentHeaderBroker = 0L;

    Map<String, List<Long>> queueHeader = new HashMap<>();

    Map<String, AtomicLong> queueIndex = new HashMap<>();

    FileChannel headerFc;

    FileChannel bodyFc;

    FileChannel headerReadFc;

    FileChannel bodyReadFc;

    public QueueStoreOne() {
        init();
    }

    void init() {
        try {
            headerFc = new FileOutputStream(PATH + "header").getChannel();
            headerReadFc = new FileInputStream(PATH + "header").getChannel();
            bodyFc = new FileOutputStream(PATH + "body").getChannel();
            bodyReadFc = new FileInputStream(PATH + "body").getChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    synchronized void put(String queueName, byte[] message) {
        try {
            long headerOffset = getHeaderOffset(queueName, true);
            ByteBuffer headerBuffer = ByteBuffer.allocateDirect(HEADER_LENGTH);
            long bodyPosition = bodyFc.position();
            headerFc.position(headerOffset);
            headerBuffer.putShort((short) message.length);
            headerBuffer.putLong(bodyPosition);
            headerBuffer.flip();
            headerFc.write(headerBuffer);
            ByteBuffer bodyBuffer = ByteBuffer.allocateDirect(message.length);
            bodyBuffer.put(message);
            bodyBuffer.flip();
            bodyFc.write(bodyBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    synchronized Collection<byte[]> get(String queueName, long offset, long num) {
        Collection<byte[]> result = new LinkedList<>();
        AtomicLong indexLong = getIndex(queueName);
        if (indexLong == null || indexLong.get() < offset) {
            return result;
        }
        long currentOffset = offset;
        long currentNum = num;
        while (currentNum > 0) {
            int ii = (int) (offset % HEADER_BROKER);
            long last = HEADER_BROKER - (long)ii;
            if (currentNum > last) {
                get(result, queueName, currentOffset, last);
                currentOffset += last;
                currentNum -= last;
            } else {
                get(result, queueName, currentOffset, currentNum);
                currentNum = -1;
            }
        }
        return result;
    }

    synchronized void get(Collection<byte[]> result, String queueName, long offset, long num) {
        try {
            AtomicLong indexLong = getIndex(queueName);
            if (indexLong == null || indexLong.get() < offset) {
                return;
            }
            if ((indexLong.get() - offset) < num) {
                num = indexLong.get() - offset;
            }
            long headerOffset = getHeaderOffset(queueName, offset);
            headerReadFc.position(headerOffset);
            ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_LENGTH * (int) num);
            headerReadFc.read(headerBuffer);
            headerBuffer.flip();
            for (int i = 0; i < num; i++) {
                short bodyLength = headerBuffer.getShort();
                long bodyAddr = headerBuffer.getLong();
                bodyReadFc.position(bodyAddr);
                ByteBuffer bodyBuffer = ByteBuffer.allocate(bodyLength);
                bodyReadFc.read(bodyBuffer);
                bodyBuffer.flip();
                result.add(bodyBuffer.array());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AtomicLong getIndex(String queueName) {
        return queueIndex.computeIfAbsent(queueName, o -> new AtomicLong(0L));
    }


    private long getHeaderOffset(String queueName, long offset) throws Exception {
        List<Long> headerIndexs = queueHeader.get(queueName);
        int i = (int) (offset / HEADER_BROKER);
        if (headerIndexs == null || headerIndexs.size() <= i) {
            throw new Exception(queueName + "-" + offset);
        }
        long indexStart =  headerIndexs.get(i);
        long indexOffset = offset % HEADER_BROKER;
        return (indexOffset + indexStart) * HEADER_LENGTH;
    }


    private long getHeaderOffset(String queueName, boolean isIncrement) {
        long index = isIncrement ? getIndex(queueName).getAndIncrement() : getIndex(queueName).get();
        int i = (int)(index / HEADER_BROKER);
        List<Long> headerBorker = queueHeader.computeIfAbsent(queueName, o -> new ArrayList<>());
        if (headerBorker.size() <= i) {
            headerBorker.add(currentHeaderBroker);
            currentHeaderBroker += HEADER_BROKER;
        }
        long headerIndex = headerBorker.get(i);
        long offset = index % HEADER_BROKER;
        return (headerIndex + offset) * HEADER_LENGTH;
    }

    public static void main (String[] args) {
        QueueStoreOne queueStoreOne = new QueueStoreOne();
        for (int i = 0; i < 10; i++) {
            queueStoreOne.put("name", ("liubolun" + i).getBytes());
            queueStoreOne.put("one", ("one" + i).getBytes());
        }
        Collection<byte[]> bytes = queueStoreOne.get("one", 12, 10);
        bytes.stream().forEach(b -> {
            String s = new String(b);
            System.out.println(s);
        });
    }



}
