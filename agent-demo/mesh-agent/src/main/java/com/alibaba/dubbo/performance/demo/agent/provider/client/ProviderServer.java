/*
 * @projectName dubbo-mesh
 * @package com.alibaba.dubbo.performance.demo.agent.provider.client
 * @className com.alibaba.dubbo.performance.demo.agent.provider.client.ProviderServer
 * @copyright Copyright 2019 Thuisoft, Inc. All rights reserved.
 */
package com.alibaba.dubbo.performance.demo.agent.provider.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * ProviderServer
 * @description ProviderServer
 * @author liubolun
 * @date 2019年12月05日 20:55
 * @version 2.9
 */
public class ProviderServer {

    private static final int SERVER_PORT = Integer.valueOf(System.getProperty("server.port"));



    public void init() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
            b.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ProviderServerInit())
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);
            Channel channel = b.bind(SERVER_PORT + 50).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {
        ProviderServer providerServer = new ProviderServer();
        providerServer.init();
    }


}
