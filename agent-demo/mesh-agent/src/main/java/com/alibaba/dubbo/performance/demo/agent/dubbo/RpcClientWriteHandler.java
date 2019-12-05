/*
 * @projectName dubbo-mesh
 * @package com.alibaba.dubbo.performance.demo.agent.dubbo
 * @className com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClientWriteHandler
 * @copyright Copyright 2019 Thuisoft, Inc. All rights reserved.
 */
package com.alibaba.dubbo.performance.demo.agent.dubbo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

/**
 * RpcClientWriteHandler
 * @description TODO
 * @author liubolun
 * @date 2019年12月04日 21:33
 * @version 2.9
 */
public class RpcClientWriteHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Promise<Integer> agentResponsePromise = new DefaultPromise<>(ctx.executor());
        agentResponsePromise.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                future.get();
            }
        });
    }
}
