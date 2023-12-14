package com.bulls.qa.common;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class YamlPropertySourceFactory implements PropertySourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(YamlPropertySourceFactory.class);
    //Properties properties;
    int i = 0;
    int requestsNo;
    int pioneersNo;
    int globalVariableNo;
    Properties properties;

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Properties propertiesFromYaml = loadYamlIntoProperties(resource);
        append(propertiesFromYaml);
        String sourceName = name != null ? name : resource.getResource().getFilename();
        //logger.info("load source:{}", sourceName);
        //logger.info("i={}", i);

        i++;
        return new PropertiesPropertySource(sourceName, properties);

       /* List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(resource.getResource().getFilename(), resource.getResource());
        return sources.get(0);*/
    }

    private void append(Properties newProperties) {
        if (i == 0) {
            properties = newProperties;
            Set<String> global = new HashSet<>();
            Set<String> pioneer = new HashSet<>();
            Set<String> request = new HashSet<>();
            for (String key : newProperties.stringPropertyNames()) {

                String[] ss = key.split("\\.");
                if (ss.length != 3) {
                    continue;
                }
                if (key.startsWith("api.globalVariable")) {

                    global.add(ss[1]);
                } else if (key.startsWith("api.pioneers")) {

                    pioneer.add(ss[1]);
                } else if (key.startsWith("api.requests")) {

                    request.add(ss[1]);
                }
            }
            globalVariableNo = global.size() - 1;
            pioneersNo = pioneer.size() - 1;
            requestsNo = request.size() - 1;
        } else {
            Map<String, String> map = new HashMap<>();

            Object[] keys = newProperties.stringPropertyNames().toArray();
            Arrays.sort(newProperties.stringPropertyNames().toArray());

            for (Object keyo : keys) {

                String key = (String) keyo;
                String value = newProperties.getProperty(key);
                String[] ss = key.split("\\.");
                String newKey = null;
                if (map.containsKey(ss[1])) {
                    newKey = ss[0] + "." + map.get(ss[1]) + "." + ss[2];
                } else {
                    if (key.startsWith("api.globalVariable")) {
                        globalVariableNo++;
                        map.put(ss[1], globalVariableNo + "");
                        newKey = ss[0] + ".globalVariable[" + globalVariableNo + "]." + ss[2];
                    } else if (key.startsWith("api.pioneers")) {
                        pioneersNo++;
                        map.put(ss[1], pioneersNo + "");
                        newKey = ss[0] + "pioneers.[" + pioneersNo + "]." + ss[2];
                    } else if (key.startsWith("api.requests")) {
                        requestsNo++;
                        map.put(ss[1], requestsNo + "");
                        newKey = ss[0] + ".requests[" + requestsNo + "]." + ss[2];
                    }
                }
                if (StringUtils.isNotEmpty(newKey)) {
                    //logger.info(key);
                    //logger.info(newKey);
                    String[] ss1 = key.split("[\\[,\\]]");
                    newKey = ss1[0] + "[" + map.get(ss[1]) + "]" + ss1[2];
                    /*logger.info(newKey);
                    logger.info("i={}", i);
                    logger.info("{}:{}", key, newProperties.getProperty(key));
                    logger.info("{}:{}", newKey, newProperties.getProperty(key));*/
                    properties.setProperty(newKey, newProperties.getProperty(key));
                    //newProperties.setProperty(newKey, newProperties.getProperty(key));
                    //newProperties.remove(key);
                }
            }

        }
    }

    private Properties loadYamlIntoProperties(EncodedResource resource) throws FileNotFoundException {
        try {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();
            return factory.getObject();
        } catch (IllegalStateException e) {
            // for ignoreResourceNotFound
            Throwable cause = e.getCause();
            if (cause instanceof FileNotFoundException)
                throw (FileNotFoundException) e.getCause();
            throw e;
        }
    }
}
