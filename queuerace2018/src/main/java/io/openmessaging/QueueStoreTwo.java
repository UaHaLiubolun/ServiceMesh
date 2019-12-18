/*
 * @projectName race2018
 * @package io.openmessaging
 * @className io.openmessaging.QueueStoreTwo
 * @copyright Copyright 2019 Thuisoft, Inc. All rights reserved.
 */
package io.openmessaging;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * QueueStoreTwo
 * @description QueueStoreTwo
 * @author liubolun
 * @date 2019年12月16日 9:52
 * @version 3.1.1
 */
public class QueueStoreTwo extends QueueStore {

    static final String PATH = "/tmp/queue-two/";

    static final long BLOCK_SIZE = 100L;

    static final long MESSAGE_SIZE = 60L;

    FileChannel fileChannel;

    ByteBuffer writeBuffer = ByteBuffer.allocateDirect(4 * 1024);

    Map<String, AtomicLong> currentIndex = new ConcurrentHashMap<>();

    Map<String, List<Long>> queueIndex = new ConcurrentHashMap<>();

    AtomicLong block = new AtomicLong(0);

    private void init() {
        try {
            fileChannel = new RandomAccessFile(PATH + "body", "rw").getChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    void put(String queueName, byte[] message) {

    }

    @Override
    Collection<byte[]> get(String queueName, long offset, long num) {
        return null;
    }

    private int getBlockIndex(long index) {
        return (int) (index / BLOCK_SIZE);
    }

    private long getBlockOffset(long index) {
        return index % BLOCK_SIZE;
    }

    private synchronized long getNewBlockAddr() {
        return block.getAndIncrement() * BLOCK_SIZE * MESSAGE_SIZE;
    }
}
