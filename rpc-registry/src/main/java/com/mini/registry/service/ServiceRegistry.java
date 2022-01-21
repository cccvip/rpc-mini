package com.mini.registry.service;

import com.mini.registry.model.ServiceMeta;

/**
 * @author carl-xiao
 * @description 服务注册中心
 **/
public interface ServiceRegistry {
    /**
     * 注册服务
     *
     * @param serviceMeta
     */
    void register(ServiceMeta serviceMeta) throws Exception;

    /**
     * 取消注册
     *
     * @param serviceMeta
     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务发现
     *
     * @param serviceName 服务名字
     * @return
     * @throws Exception
     */
    ServiceMeta discovery(String serviceName) throws Exception;

    /**
     * 服务节点销毁
     *
     * @throws Exception
     */
    void destroy() throws Exception;
}
