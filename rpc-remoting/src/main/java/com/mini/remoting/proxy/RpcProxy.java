package com.mini.remoting.proxy;

import com.mini.model.MiniRpcRequest;
import com.mini.model.MiniRpcResponse;
import com.mini.protocol.*;
import com.mini.registry.service.ServiceRegistry;
import com.mini.remoting.AbstractClient;
import com.mini.remoting.netty.NettyRpcClient;
import com.mini.serialization.SerializationTypeEnum;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

/**
 * @author carl-xiao
 * @description 代理类
 **/
public class RpcProxy implements InvocationHandler {

    private final String serviceVersion;
    private final long timeout;
    private final ServiceRegistry registryService;
    private final AbstractClient rpcRequestTransport;

    public RpcProxy(String serviceVersion, long timeout,
                    ServiceRegistry registryService, AbstractClient abstractClient) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.registryService = registryService;
        this.rpcRequestTransport = abstractClient;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MsgProtocol<MiniRpcRequest> protocol = new MsgProtocol<>();
        //header
        MsgHeader header = new MsgHeader();
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstant.MAGIC);
        header.setVersion(ProtocolConstant.VERSION);
        header.setRequestId(requestId);
        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        header.setMsgType((byte) MsgType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);
        //body
        MiniRpcRequest request = new MiniRpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParams(args);
        protocol.setBody(request);
        //send请求
        MiniRpcResponse rpcResponse = null;
        if (rpcRequestTransport instanceof NettyRpcClient) {
            CompletableFuture<MiniRpcResponse> completableFuture = rpcRequestTransport.sendRpcRequest(protocol, registryService);
            rpcResponse = completableFuture.get();
        }
        return rpcResponse.getData();
    }
    /**
     * 代理类初始化
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }
}
