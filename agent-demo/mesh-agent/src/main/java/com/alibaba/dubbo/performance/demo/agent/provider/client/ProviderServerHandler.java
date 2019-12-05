/*
 * @projectName dubbo-mesh
 * @package com.alibaba.dubbo.performance.demo.agent.provider.client
 * @className com.alibaba.dubbo.performance.demo.agent.provider.client.ProviderServerHandler
 * @copyright Copyright 2019 Thuisoft, Inc. All rights reserved.
 */
package com.alibaba.dubbo.performance.demo.agent.provider.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.ConnecManager;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.Request;
import com.alibaba.dubbo.performance.demo.agent.dubbo.model.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.util.RequestParser;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProviderServerHandler
 * @description TODO
 * @author liubolun
 * @date 2019年12月05日 21:20
 * @version 2.9
 */
public class ProviderServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");

    public static ConnecManager connectManager = new ConnecManager();

    public static Map<String, Promise<RpcResponse>> promiseMap = new ConcurrentHashMap<>();



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        Request request = RequestParser.getRequest(msg);
        Promise<RpcResponse> promise = new DefaultPromise<>(ctx.executor());
        promiseMap.put(String.valueOf(request.getId()), promise);
        promise.addListener((f) -> {
            RpcResponse rpcResponse = (RpcResponse) f.get();
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(rpcResponse.getBytes()));
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
            promiseMap.remove(rpcResponse.getRequestId());
            ctx.writeAndFlush(response);
        });
        connectManager.getChannel().writeAndFlush(request);
    }
}
