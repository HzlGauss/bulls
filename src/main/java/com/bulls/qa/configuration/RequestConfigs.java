package com.bulls.qa.configuration;

import com.bulls.qa.common.YamlPropertySourceFactory;
import com.bulls.qa.service.PioneerService;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Data
@Configuration
@Component
@PropertySource(factory = YamlPropertySourceFactory.class, value = {"testerhome.yml"})
//@PropertySource(factory = YamlPropertySourceFactory.class, value = {"interface_*.yml"})
//@PropertySource(factory = YamlPropertySourceFactory.class, value = {"request-copy.yml"},name = "b" ,ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "api", ignoreInvalidFields = true, ignoreUnknownFields = true)
//@ConfigurationProperties(ignoreUnknownFields = false)
public class RequestConfigs {

    @Setter
    @Getter
    private List<PioneerConfig> pioneers;

    //@Setter
    //@Getter
    private List<RequestConfig> requests;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private Map<String, Object> globalVariableMap = new HashMap<>();

    private List<Map<String, String>> globalVariables;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private static Map<String, RequestConfig> requestMap = new HashMap<>();


    @Autowired
    Environment environment;

    private Properties properties;

    public Map<String, String> reLogin(String cookiesName) {
        for (PioneerConfig pioneerConfig : pioneers) {
            for (Map<String, String> map : pioneerConfig.getExtractors()) {
                System.out.println(map.keySet());
                if (map.get("name").equals(cookiesName)) {
                    PioneerService.handle(pioneerConfig, this);
                    Map<String, String> res = (Map<String, String>) this.getGlobalVariable(cookiesName);
                    System.out.println(res);
                    return res;
                }
            }
        }
        return null;
    }

    public void setGlobalVariables(List<Map<String, String>> globalVariables) {
        //this.globalVariables = globalVariables;
        for (Map<String, String> map : globalVariables) {
            this.globalVariableMap.putAll(map);
        }
    }

    public void setRequests(List<RequestConfig> requests) {
        if (this.requests == null) {
            this.requests = requests;
        } else {
            this.requests.addAll(requests);
        }
    }

    //@Bean(name = "envProperties")
    public Properties getEnvProperties() {
        if (this.properties != null) {
            return this.properties;
        }
        //ApplicationContext context = new AnnotationConfigApplicationContext(SConfig.class);
        //String[] ss = context.getEnvironment().getActiveProfiles();
        String[] ss = environment.getActiveProfiles();
        if (ss.length > 0) {
            String env = ss[0];
            Resource resource = new ClassPathResource(env + "/test.properties");
            Properties props = null;
            try {
                props = PropertiesLoaderUtils.loadProperties(resource);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return props;
        }
        return null;
    }

    public void setGlobalVariable(String name, Object value) {
        this.globalVariableMap.put(name, value);
    }

    public Object getGlobalVariable(String name) {
        if (this.globalVariableMap.containsKey(name)) {
            return this.globalVariableMap.get(name);
        }
        Properties properties = this.getEnvProperties();
        if (properties != null) {
            return properties.getProperty(name);
        }
        return null;
    }

    public RequestConfig getRequest(String requestId) {

        if (requestMap.keySet().contains(requestId)) {
            return requestMap.get(requestId);
        }
        for (RequestConfig request : requests) {
            if (request.getId().equals(requestId)) {
                requestMap.put(requestId, request);
                return request;
            }
        }
        if (this.pioneers == null) {
            return null;
        }
        for (RequestConfig request : this.pioneers) {
            if (request.getId().equals(requestId)) {
                requestMap.put(requestId, request);
                return request;
            }
        }
        return null;
    }
}
