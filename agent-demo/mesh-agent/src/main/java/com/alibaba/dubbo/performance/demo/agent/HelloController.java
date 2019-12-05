package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import okhttp3.*;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultRequest;
import org.asynchttpclient.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(HelloController.class);

    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    private AsyncHttpClient asyncHttpClient = org.asynchttpclient.Dsl.asyncHttpClient();


    private RpcClient rpcClient = new RpcClient(registry);
    private Random random = new Random();
    private List<Endpoint> endpoints = null;
    private Object lock = new Object();
    private OkHttpClient httpClient = new OkHttpClient();


    @RequestMapping(value = "")
    public Object invoke(@RequestParam("interface") String interfaceName,
                         @RequestParam("method") String method,
                         @RequestParam("parameterTypesString") String parameterTypesString,
                         @RequestParam("parameter") String parameter) throws Exception {
        String type = System.getProperty("type");   // 获取type参数
        if ("consumer".equals(type)){
            return consumer(interfaceName,method,parameterTypesString,parameter);
        }
        else if ("provider".equals(type)){
            return provider(interfaceName,method,parameterTypesString,parameter);
        }else {
            return "Environment variable type is needed to set to provider or consumer.";
        }
    }

    public byte[] provider(String interfaceName,String method,String parameterTypesString,String parameter) throws Exception {

        Object result = rpcClient.invoke(interfaceName,method,parameterTypesString,parameter);
        return (byte[]) result;
    }

    public DeferredResult<ResponseEntity> consumer(String interfaceName,String method,String parameterTypesString,String parameter) throws Exception {

        if (null == endpoints){
            synchronized (lock){
                if (null == endpoints){
                    endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                }
            }
        }

        // 简单的负载均衡，随机取一个
        Endpoint endpoint = endpoints.get(random.nextInt(endpoints.size()));

        String url =  "http://" + endpoint.getHost() + ":" + (endpoint.getPort() + 50);

        org.asynchttpclient.Request request = org.asynchttpclient.Dsl.post(url)
                .addFormParam("interface", interfaceName)
                .addFormParam("method", method)
                .addFormParam("parameterTypesString", parameterTypesString)
                .addFormParam("parameter", parameter)
                .build();

        DeferredResult<ResponseEntity> result = new DeferredResult<>();


        //
//        RequestBody requestBody = new FormBody.Builder()
//                .add("interface",interfaceName)
//                .add("method",method)
//                .add("parameterTypesString",parameterTypesString)
//                .add("parameter",parameter)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();

        ListenableFuture<org.asynchttpclient.Response> responseFuture = asyncHttpClient.executeRequest(request);

        Runnable callback = () -> {
            try {
                String value = responseFuture.get().getResponseBody();
                ResponseEntity responseEntity = new ResponseEntity(value, HttpStatus.OK);
                result.setResult(responseEntity);
            } catch (Exception e){
                e.printStackTrace();
            }
        };

        responseFuture.addListener(callback, null);

        return result;




//        try (Response response = httpClient.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            byte[] bytes = response.body().bytes();
//            String s = new String(bytes);
//            return Integer.valueOf(s);
//        }
    }
}
