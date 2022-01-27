package com.mini.spring.config;

import com.mini.spring.CustomScannerRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author carl-xiao
 * @description
 **/
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcAutoConfiguration {
    @Resource
    private RpcProperties rpcProperties;

    @Bean
    @ConditionalOnBean(CustomScannerRegistrar.class)
    public CustomScannerRegistrar init() {
        return new CustomScannerRegistrar(rpcProperties.getBasePackage());
    }
}
