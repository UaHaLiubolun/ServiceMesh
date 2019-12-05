package com.alibaba.dubbo.performance.demo.agent.dubbo;

import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcFuture;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;

import com.alibaba.dubbo.performance.demo.agent.provider.client.ProviderServerHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

//    @Override
//    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) {
//        String requestId = response.getRequestId();
//        RpcFuture future = RpcRequestHolder.get(requestId);
//        if(null != future){
//            RpcRequestHolder.remove(requestId);
//            future.done(response);
//        }
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) {
        String requestId = response.getRequestId();
        Promise<RpcResponse> promise = ProviderServerHandler.promiseMap.get(requestId);
        if (promise != null) {
            promise.trySuccess(response);
        }
    }
}
