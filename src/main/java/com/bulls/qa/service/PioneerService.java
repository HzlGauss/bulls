package com.bulls.qa.service;

import com.bulls.qa.configuration.RequestConfigs;
import com.bulls.qa.request.Request;
import com.bulls.qa.configuration.PioneerConfig;
import io.restassured.RestAssured;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.spi.AuthFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class PioneerService {
    @Autowired
    private RequestConfigs requestConfigs;
    @Value("${miria.cookie:abc}")
    String buildinCookie;

    @Autowired
    private Environment env;

    private static Environment staticEnv;

    public static String getProperty(String key) {
        return staticEnv.getProperty(key);
    }

    public static void handle(PioneerConfig pioneerConfig, RequestConfigs requestConfigs) {
        Request request = Request.getInstance(pioneerConfig);
        Response response = request.doRequest();
        String cookiesName = null;
        Map<String, String> cookies = new HashMap<>();
        for (Map<String, String> extractor : pioneerConfig.getExtractors()) {
            String name = extractor.get("name");
            String value = extractor.get("value");
            if (name.equals("appendCookie")) {
                String[] ss = value.split("=");
                if (ss.length == 2) {
                    if (StringUtils.isNotEmpty(ss[1])) {
                        //通过配置变量引用添加
                        if (ss[1].startsWith("$")) {
                            String k = ss[1].replace("$", "");
                            if (cookies != null) {
                                Object v = requestConfigs.getGlobalVariable(k);
                                cookies.put(ss[0], v.toString());
                                requestConfigs.setGlobalVariable(cookiesName, cookies);
                            }
                            //直接添加
                        } else {
                            cookies.put(ss[0], ss[1]);
                            requestConfigs.setGlobalVariable(cookiesName, cookies);
                        }
                    }
                }
            } else if (value.equals("cookies")) {
                cookies.putAll(response.getCookies());
                if (requestConfigs.getGlobalVariable(name) != null) {
                    ((Map) requestConfigs.getGlobalVariable(name)).putAll(cookies);
                } else {
                    requestConfigs.setGlobalVariable(name, cookies);
                }
                cookiesName = name;
            } else if (StringUtils.isNotEmpty(value) && value.startsWith("$.")) {
                value = value.replace("$.", "");
                Object o = response.jsonPath().get(value);

                if (o != null) {
                    //System.out.println(o);
                    requestConfigs.setGlobalVariable(name, o);
                }
            }
        }
    }

    @PostConstruct
    private void prepareAction() {
        //initRequest();
        PioneerService.staticEnv = this.env;
        if (Request.getRequestConfigs() == null) {
            //return;
            Request.setRequestConfigs(requestConfigs);
        }
        List<PioneerConfig> pioneers = requestConfigs.getPioneers();
        if (pioneers == null) {
            return;
        }
        Collections.sort(pioneers, new Comparator<PioneerConfig>() {
            @Override
            public int compare(PioneerConfig o1, PioneerConfig o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });
        for (PioneerConfig pioneerConfig : pioneers) {
            handle(pioneerConfig, this.requestConfigs);
        }
    }

    //多场景支持
    private void initRequest() {
        if (StringUtils.isNotEmpty(buildinCookie)) {
            System.out.println(buildinCookie);
            RestAssured.filters(new AuthFilter() {
                @Override
                public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                    if (buildinCookie.contains("=") || buildinCookie.contains(":")) {
                        String[] ss = buildinCookie.split("=|:");
                        requestSpec.cookies(ss[0], ss[1]);
                    }
                    return ctx.next(requestSpec, responseSpec);
                }
            });
        }

    }
}
