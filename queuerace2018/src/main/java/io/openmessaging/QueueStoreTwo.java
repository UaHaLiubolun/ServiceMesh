/*
 * @projectName race2018
 * @package io.openmessaging
 * @className io.openmessaging.QueueStoreTwo
 * @copyright Copyright 2019 Thuisoft, Inc. All rights reserved.
 */
package io.openmessaging;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Collection;

/**
 * QueueStoreTwo
 * @description QueueStoreTwo
 * @author liubolun
 * @date 2019年12月16日 9:52
 * @version 3.1.1
 */
public class QueueStoreTwo extends QueueStore {

    final static String PATH = "/tmp/queue-two/";

    FileChannel fileChannel;

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
}
