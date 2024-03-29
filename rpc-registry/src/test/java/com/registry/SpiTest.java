package com.registry;

import com.mini.extention.ExtensionLoader;
import com.mini.registry.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author carl-xiao
 * @description
 **/
public class SpiTest {
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    @Test
    public void SpiTest() {
        String fileName = SERVICE_DIRECTORY + "com.mini.registry.service.ServiceRegistry";
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    System.out.println(resourceUrl);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("end");
//        ServiceRegistry serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
//        System.out.println(serviceRegistry.toString());
    }
}
