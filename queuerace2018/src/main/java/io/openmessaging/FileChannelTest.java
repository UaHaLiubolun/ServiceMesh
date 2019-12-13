/*
 * @projectName race2018
 * @package io.openmessaging
 * @className io.openmessaging.FileChannelTest
 * @copyright Copyright 2019 Thuisoft, Inc. All rights reserved.
 */
package io.openmessaging;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * FileChannelTest
 * @description FileChannelTest
 * @author liubolun
 * @date 2019年12月12日 11:00
 * @version 3.1.1
 */
public class FileChannelTest {


    final static String filePath = "/tmp/filechannel";

    public static void main(String[] args) throws Exception {
        File file = new File(filePath);
        FileChannel fileChannel = new FileOutputStream(file).getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put("hahahh".getBytes());
        byteBuffer.flip();
        fileChannel.write(byteBuffer);
        byteBuffer.position(0);
        fileChannel.write(byteBuffer, byteBuffer.limit());
    }
}
