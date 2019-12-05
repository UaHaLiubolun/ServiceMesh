/*
 * @projectName dubbo-mesh
 * @package com.alibaba.dubbo.performance.demo.agent.provider.client
 * @className com.alibaba.dubbo.performance.demo.agent.provider.client.ProviderServerInit
 * @copyright Copyright 2019 Thuisoft, Inc. All rights reserved.
 */
package com.alibaba.dubbo.performance.demo.agent.provider.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * ProviderServerInit
 * @description ProviderServerInit
 * @author liubolun
 * @date 2019年12月05日 21:19
 * @version 2.9
 */
public class ProviderServerInit extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("aggregator", new HttpObjectAggregator(10 * 1024 * 1024));
        p.addLast(new ProviderServerHandler());
    }
}
