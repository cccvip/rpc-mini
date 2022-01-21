package com.mini.registry.service.impl;

import com.mini.registry.model.ServiceMeta;
import com.mini.registry.service.RpcServiceHelper;
import com.mini.registry.service.ServiceRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

/**
 * @author carl-xiao
 * @description zk注册服务
 **/
public class ZookeeperRegistry implements ServiceRegistry {

    /**
     * 间隔休眠
     */
    private final int BASE_SLEEP_TIME_MS = 1000;
    /**
     * 最大重试次数
     */
    private final int MAX_RETRIES = 3;
    /**
     * zk base路径
     */
    private final String ZK_BASE_PATH = "/rpc";
    /**
     * 注册中心
     */
    private final ServiceDiscovery<ServiceMeta> serviceDiscovery;

    public ZookeeperRegistry(String registryAddr) throws Exception {
        /**
         * 启动zk会话
         */
        CuratorFramework client = CuratorFrameworkFactory.newClient(registryAddr,
                new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();
        /**
         * 服务注册
         */
        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_BASE_PATH)
                .build();
        this.serviceDiscovery.start();
    }

    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        //服务昵称
        String serviceName = serviceMeta.getServiceName();
        //服务版本号
        String serviceVersion = serviceMeta.getServiceVersion();
        String builderName = RpcServiceHelper.buildServiceKey(serviceName, serviceVersion);

        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance.<ServiceMeta>builder()
                .name(builderName)
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        String serviceName = serviceMeta.getServiceName();
        //服务版本号
        String serviceVersion = serviceMeta.getServiceVersion();
        String builderName = RpcServiceHelper.buildServiceKey(serviceName, serviceVersion);
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance.<ServiceMeta>builder()
                .name(builderName)
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public ServiceMeta discovery(String serviceName) throws Exception {


        return null;
    }

    @Override
    public void destroy() throws Exception {
        serviceDiscovery.close();
    }

}
