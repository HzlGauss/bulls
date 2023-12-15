package com.bulls.qa.request;

import com.bulls.qa.configuration.RequestConfigs;
import com.bulls.qa.configuration.SConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.bulls.qa.configuration.RequestConfig;
import com.bulls.qa.service.PioneerService;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import org.testng.Reporter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static io.restassured.RestAssured.given;

@Slf4j
public class Request {
    private RequestConfig requestConfig;
    private String method;
    private String name;
    private String path;
    private String filePath;
    private Map<String, Object> headers;
    private Map<String, String> cookies = new HashMap<>();
    private Map<String, Object> parameter;
    private Map<String, Object> tmpParameter = new HashMap<>();
    //private static final Logger logger = LoggerFactory.getLogger(RequestConfig.class);
    //private static final Logger logger= LogManager.getLogger(Request.class);
    private static final Logger logger = LoggerFactory.getLogger(Request.class);

    public static void setRequestConfigs(RequestConfigs requestConfigs) {
        Request.requestConfigs = requestConfigs;
    }

    public static RequestConfigs getRequestConfigs() {
        return requestConfigs;
    }


    private static RequestConfigs requestConfigs = null;


    private Request(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        init();
    }

    public Request setCookies(Map<String, String> cookies) {
        if (this.cookies == null) {
            this.cookies = cookies;
        } else {
            this.cookies.putAll(cookies);
        }
        return this;
    }

    public Request clearCookies() {
        if (this.cookies != null) {
            this.cookies.clear();
        }
        return this;
    }

    public void addCookie(String key, String value) {
        this.cookies.put(key, value);
    }

    public void addCookies(Map<String, String> cookies) {
        if (cookies == null || cookies.size() == 0) {
            return;
        }
        if (this.headers != null && this.headers.containsKey("cookie")) {
            Object cookie = this.headers.get("cookie");
            this.cookies.putAll((Map<? extends String, ? extends String>) cookie);
            this.headers.remove("cookie");
        }
        this.cookies.putAll(cookies);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private void init() {
        String requestMethod = requestConfig.getMethod().trim();
        if (requestMethod.equalsIgnoreCase("get")) {
            this.method = "get";
        } else if (requestMethod.equalsIgnoreCase("post")) {
            this.method = "post";
        }
        this.method = requestConfig.getMethod();
        this.name = requestConfig.getName();
        this.path = requestConfig.getPath();
        this.filePath = requestConfig.getFilePath();
        try {
            initPath();
            initHeader();
            initParameter();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
        initGlobalVariable();
    }

    private void initPath() {
        try {
            URL url = new URL(this.path);
            String host = url.getHost();
            if (host.startsWith("$")) {
                String value = PioneerService.getProperty(host.substring(1));
                String replace = this.path.replace(host, value);
                this.path = replace;
                logger.info("{}", replace);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void initHeader() throws IOException {
        ObjectMapper MAPPER = new ObjectMapper();
        String s = requestConfig.getHeaders();
        if (StringUtils.isNotEmpty(s)) {
            HashMap<String, Object> headers = MAPPER.readValue(s.replace("\\", ""), HashMap.class);
            for (String key : headers.keySet()) {
                Object value = headers.get(key);
                if (value instanceof String) {
                    String sv = (String) value;
                    if (StringUtils.isNotEmpty(sv) && sv.startsWith("$")) {
                        Object o = requestConfigs.getGlobalVariable(sv.replace("$", ""));
                        if (o != null) {
                            headers.put(key, o);
                        }
                    }
                }
            }
            //cookie
            if (headers.containsKey("cookie")) {
                Object value = headers.get("cookie");
                Map<String, String> cookie = null;
                if (value instanceof String) {
                    cookie = string2Map((String) value);
                } else {
                    cookie = (Map<String, String>) value;
                }
                if (this.cookies == null) {
                    this.cookies = cookie;
                } else {
                    this.cookies.putAll(cookie);
                }
                headers.remove("cookie");
            }
            this.headers = headers;
        }
    }

    private static Map<String, String> string2Map(String str) {
        Map<String, String> map = new HashMap<String, String>();
        if (str == null || str.trim().length() == 0) {
            return map;
        }
        for (String subStr : str.split(";")) {
            String[] kv = subStr.split("=");
            if (kv.length != 2) {
                continue;
            }
            map.put(kv[0], kv[1]);
        }
        return map;
    }

    private void initParameter() throws IOException {
        ObjectMapper MAPPER = new ObjectMapper();
        if (StringUtils.isNotEmpty(requestConfig.getParameters())) {
            String s = requestConfig.getParameters().replace("\\", "").trim();
            HashMap<String, Object> parameters = null;
            if (s.startsWith("{") && s.endsWith("}")) {
                parameters = MAPPER.readValue(s, HashMap.class);
            } else {
                parameters = new HashMap<>();
                String[] ss = s.split("[=&]");
                int len = ss.length;
                if ((len & 0x01) == 1) {
                    len--;
                }
                for (int i = 0; i < len; i = i + 2) {
                    if (ss[i + 1].contains(",")) {
                        parameters.put(ss[i], Arrays.asList(ss[i + 1].split(",")));
                    } else {
                        parameters.put(ss[i], ss[i + 1]);
                    }

                }
            }
            this.parameter = parameters;
        } else {
            this.parameter = new HashMap<>();
        }
    }

    private void initGlobalVariable() {
        //cookies
        String cookiesName = this.requestConfig.getCookies();
        if (StringUtils.isNotEmpty(cookiesName)) {
            String[] names = cookiesName.split(";");
            for (String name : names) {
                if (name.startsWith("$")) {
                    name = name.replace("$", "");
                    if (requestConfigs.getGlobalVariable(name) != null) {
                        Object value = requestConfigs.getGlobalVariable(name);
                        if (value instanceof Map) {
                            this.cookies.putAll((Map<String, String>) value);
                        } else {
                            this.cookies.put(name, value.toString());
                        }
                    }
                }
            }
            /*if (cookiesName.startsWith("$")) {
                cookiesName = cookiesName.replace("$", "");
                if (requestConfigs.getGlobalVariable(cookiesName) != null) {
                    this.cookies.putAll((Map<String, String>) requestConfigs.getGlobalVariable(cookiesName));
                }
            }*/
        }
        //host
        if (StringUtils.isEmpty(this.path)) {
            logger.error("{}-{}path格式错误，无法完成请求，请核对配置", requestConfig.getName(), requestConfig.getId());
            return;
        }
        String s = this.path;
        s = s.replace("http://", "").replace("https://", "");//去除http和https前缀
        String[] arr = s.split("/");//按‘/’分隔，取第一个
        String host = arr[0];
        if (host.startsWith("$")) {
            host = host.replace("$", "");
            host = (String) requestConfigs.getGlobalVariable(host);
            if (StringUtils.isNotEmpty(host)) {
                this.path.replace(arr[0], host);
            }
        }
        //header
        if (this.headers != null) {
            for (String key : this.headers.keySet()) {
                Object value = this.headers.get(key);
                if (value instanceof String) {
                    String s1 = (String) value;
                    if (StringUtils.isNotEmpty(s1) && s.startsWith("$")) {
                        String v = (String) requestConfigs.getGlobalVariable(s1.replace("$", ""));
                        if (StringUtils.isNotEmpty(v)) {
                            this.headers.put(key, v);
                        }
                    }
                }
            }
        }
        //parameter
        if (this.parameter != null && this.parameter.size() > 0) {
            mapReplace(this.parameter);
        }
    }


    private void mapReplace(Map<String, Object> map) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value != null) {
                if (value instanceof java.util.List) {
                    List list = (List) value;
                    for (int i = 0; i < list.size(); i++) {
                        Object o = list.get(i);
                        if (o instanceof java.util.Map) {
                            mapReplace((Map<String, Object>) o);
                        } else if (o instanceof String) {
                            String s = (String) o;
                            if (StringUtils.isNotEmpty(s) && s.startsWith("$")) {
                                s = s.replace("$", "");
                                if (StringUtils.isNotEmpty(s) && Request.requestConfigs.getGlobalVariable(s) != null) {
                                    Object o1 = Request.requestConfigs.getGlobalVariable(s);
                                    list.set(i, o1);
                                }
                            }
                        }
                    }
                } else if (value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    Object[] os = new Object[length];
                    for (int i = 0; i < length; i++) {
                        Object o = Array.get(value, i);
                        if (o instanceof java.util.Map) {
                            mapReplace((Map<String, Object>) o);
                        } else if (o instanceof String) {
                            String s = (String) o;
                            if (StringUtils.isNotEmpty(s) && s.startsWith("$")) {
                                s = s.replace("$", "");
                                if (StringUtils.isNotEmpty(s) && Request.requestConfigs.getGlobalVariable(s) != null) {
                                    Object o1 = Request.requestConfigs.getGlobalVariable(s);
                                    Array.set(value, i, o1);
                                }
                            }
                        }
                    }
                } else if (value instanceof java.util.Map) {
                    mapReplace((Map<String, Object>) value);
                } else if (value instanceof String) {
                    String s = (String) value;
                    if (StringUtils.isNotEmpty(s) && s.startsWith("$")) {
                        s = s.replace("$", "");
                        if (StringUtils.isNotEmpty(s) && Request.requestConfigs.getGlobalVariable(s) != null) {
                            Object o1 = Request.requestConfigs.getGlobalVariable(s);
                            map.put(key, o1);
                        }
                    }
                }
            }
        }
    }

    private void checkHeaderAndParam() throws Exception {
        if (this.headers == null) {
            logger.warn("header is null!");
        }
        for (String key : headers.keySet()) {
            Object value = this.headers.get(key);
            if (StringUtils.isNotEmpty(value.toString()) && value.toString().trim().equals("***")) {
                logger.warn("header {} is required!", key);
            }
        }
        if (StringUtils.isEmpty(path) || StringUtils.isEmpty(method)) {
            throw new Exception("request定义错误，缺少path或method字段");
        }
    }

    public static Request getInstance(String configId) {
        RequestConfig requestConfig = requestConfigs.getRequest(configId);
        return new Request(requestConfig);
    }

    public static Request getInstance(RequestConfig requestConfig) {
        return new Request(requestConfig);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void addHeader(Map<String, Object> headers) {
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            if ("Set-Cookie".equals(entry.getKey())) {
                continue;
            } else {
                this.headers.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void addHeaders(Headers headers) {
        Map<String, Object> myHeaders = new HashMap<String, Object>();
        List<Header> list = headers.asList();
        for (Header header : list) {
            String name = header.getName();
            String value = header.getValue();
            myHeaders.put(name, value);
        }
        if (myHeaders.size() > 0) {
            this.addHeader(myHeaders);
        }
    }

    private void updateCookie(RequestSpecification request) {
        if (this.cookies != null && this.cookies.size() > 0) {
            request = request.cookies(this.cookies);
        }
    }

    public Response doRequest() {
        Response response = null;
        try {
            checkHeaderAndParam();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        RequestSpecification request = given();
        request = request.relaxedHTTPSValidation();
        if (this.cookies != null && this.cookies.size() > 0) {
            request = request.cookies(this.cookies);
        }
        if (this.headers != null && this.headers.size() > 0) {
            request = request.headers(this.headers);
        }
        String type = (String) this.headers.get("Content-Type");
        ContentType contentType = null;
        if (StringUtils.isNotEmpty(type)) {
            if (type.contains("application/json") || type.contains("javascript")) {
                contentType = ContentType.JSON;
            } else if (type.contains("text/html")) {
                contentType = ContentType.HTML;
            } else if (type.contains("application/x-www-form-urlencoded")) {
                contentType = ContentType.URLENC;
            } else if (type.contains("multipart/form-data")) {
                if (StringUtils.isEmpty(this.filePath)) {
                    logger.error("{}文件路径不能为空！", this.requestConfig.getId());
                } else {
                    request.multiPart(new File(this.filePath));
                }
                //contentType = ContentType.BINARY;

            } else if (type.contains("text/plain")) {
                contentType = ContentType.TEXT;
            } else if (type.contains("octet-stream")) {
                contentType = ContentType.BINARY;
            } else if (type.contains("xml")) {
                contentType = ContentType.XML;
            } else {
                contentType = ContentType.ANY;
            }
            //request = request.contentType(contentType);
        } else {
            contentType = ContentType.URLENC;

        }
        if (null != type && type.contains("multipart/form-data")) {
            request = request.contentType("multipart/form-data");
        } else if (null != type && type.contains(";charSet")) {
            request = request.contentType(contentType + ";charSet" + type.split("charSet")[1]);
        } else {
            //request = request.contentType(contentType);
            request = request.contentType(type);
        }

        if (this.parameter != null && this.parameter.size() > 0) {
            if (contentType.equals(ContentType.JSON)) {
                request = request.body(this.parameter);
            } else {
                request = request.params(this.parameter);
            }
        }
        String requestInfo = String.format("[接口请求信息] id:%s,name:%s,url:%s", requestConfig.getId(),
                requestConfig.getName(), this.path);
        logger.info(requestInfo);
        Reporter.log(requestInfo);
        if (this.headers != null) {
            logger.info("请求头:{}", this.headers);
        }
        if (this.cookies != null) {
            logger.info("请求cookies:{}", this.cookies);
        }
        if (this.parameter != null && this.parameter.size() > 0) {
            logger.info("请求参数:{}", this.parameter);
            Reporter.log("请求参数:" + this.parameter);
        }
        if (this.tmpParameter != null && this.tmpParameter.size() > 0) {
            logger.info("请求参数:{}", this.tmpParameter);
            Reporter.log("请求参数:" + this.tmpParameter);
        }
        boolean isTestMethodCalled = isTestMethodCalled();
        if (isTestMethodCalled) {
            //Allure.step(requestConfig.getName() + "-请求url:" + this.path);
            //Allure.step(requestConfig.getName() + "-请求参数:" + this.parameter);
        }

        response = request(request);
        //重试
        if ((300 <= response.statusCode() && response.statusCode() < 400) || response.asString().contains("重新登录")) {
            //重新登录
            logger.info("重新登录");
            relogin();
            //重新发送请求
            updateCookie(request);
            response = request(request);
        }
        String source = response.asString();
        if (StringUtils.isNotEmpty(source) && source.contains("<title>登录</title>")) {
            //重新登录
            logger.info("重新登录");
            relogin();
            //重新发送请求
            updateCookie(request);
            response = request(request);
        }
        logger.info("name:{}, id:{},请求结果，状态码：{}；内容：{}", requestConfig.getName(), requestConfig.getId(), response.statusCode(), response.asString());
        String responseInfo = String.format("[接口返回信息] id:%s,name:%s, 返回状态码:%s", requestConfig.getId(), requestConfig.getName(), response.statusCode());
        Reporter.log(responseInfo);
        if (response != null && response.getHeaders() != null) {
            log.info("返回头:{}", response.getHeaders());
        }
        //Allure.addAttachment("log",response.asString());
        if (isTestMethodCalled) {
            //Allure.step(requestConfig.getName() + "-请求返回：" + response.asString());
        }
        return response;
    }

    private void relogin() {
        String cookiesName = this.requestConfig.getCookies();
        if (StringUtils.isNotEmpty(cookiesName)) {
            String[] names = cookiesName.split(";");
            for (String name : names) {
                if (StringUtils.isNotEmpty(name) && name.startsWith("$")) {
                    name = name.substring(1, name.length());
                    this.setCookies(requestConfigs.reLogin(name));
                }
            }
        }
    }

    private Response request(RequestSpecification request) {
        Response response = null;
        this.method = this.method.toLowerCase();
        switch (this.method) {
            case "get":
                response = request.get(this.path);
                break;
            case "post":
                response = request.post(this.path);
                break;
            case "delete":
                response = request.delete(this.path);
                break;
        }
        return response;
    }

    private static void writeValue(Object value, ArrayNode node) {
        String type = value.getClass().toString();
        switch (type) {
            case "class java.lang.String":
                node.add((String) value);
                break;
            case "class java.lang.Byte":
                node.add((Byte) value);
                break;
            case "class java.lang.Short":
                node.add((Short) value);
                break;
            case "class java.lang.Integer":
                node.add((Integer) value);
                break;
            case "class java.lang.Long":
                node.add((Long) value);
                break;
            case "class java.lang.Float":
                node.add((Float) value);
                break;
            case "class java.lang.Double":
                node.add((Double) value);
                break;
            case "class java.lang.Boolean":
                node.add((Boolean) value);
                break;
            case "class java.lang.Character":
                node.add((Character) value);
                break;
            default:
                System.out.println("不支持的类型：" + type);
        }
    }

    public Request setPath(String path) {
        this.path = path;
        return this;
    }

    public Request setParameter(String name, Object value) {
        this.tmpParameter.put(name, value);
        this.setParameters(this.tmpParameter);
        return this;
    }

    //根据路径删除，路径按json path
    public Request removeParameterByPath(String path) {
        if (!path.trim().startsWith("$.")) {
            logger.error("删除参数失败！参数路径格式错误!{}", path);
            return this;
        }
        Object map = this.parameter;
        String[] ps = path.split("\\.");
        for (int i = 1; i < ps.length; i++) {
            String key = ps[i];
            if (key.contains("[") && key.endsWith("]")) {
                String[] ss = key.split("[\\[,\\]]");
                if (!((Map<String, Object>) map).containsKey(ss[0])) {
                    logger.error("删除参数失败！通过该路径未能找到要删除参数！{}", path);
                    return this;
                }
                map = ((Map<String, Object>) map).get(ss[0]);
                int j = Integer.valueOf(ss[1]);
                if (map instanceof List && ((List) map).size() > j) {
                    //path已经到末尾
                    if (i == ps.length - 1) {
                        ((List) map).remove(j);
                    } else {
                        map = ((List) map).get(j);
                    }
                } else {
                    logger.error("设置参数失败！通过该路径未能找到要替换参数！{}", path);
                    return this;
                }
            } else {
                if (!((Map<String, Object>) map).containsKey(key)) {
                    logger.error("设置参数失败！通过该路径未能找到要替换参数！{}", path);
                    return this;
                }
                //path已经到末尾
                if (i == ps.length - 1) {
                    ((Map<String, Object>) map).remove(key);
                } else {
                    map = ((Map<String, Object>) map).get(key);
                }
            }
        }
        return this;
    }

    //根据路径设置，路径按json path
    private void setParameterByPath(String path, Object value) {
        if (!path.trim().startsWith("$.")) {
            logger.error("设置参数失败！参数路径格式错误!{}", path);
            return;
        }
        Object map = this.parameter;
        String[] ps = path.split("\\.");
        if (ps.length == 1) {
            if (map instanceof Map && value instanceof Map) {
                ((Map) map).putAll((Map) value);
            }
            if (map instanceof List) {
                ((List) map).add(value);
            }
        }
        for (int i = 1; i < ps.length; i++) {
            String key = ps[i];
            if (key.equals("$")) {
                continue;
            }
            if (key.contains("[") && key.endsWith("]")) {
                String[] ss = key.split("[\\[,\\]]");
                if (!((Map<String, Object>) map).containsKey(ss[0])) {
                    logger.error("设置参数失败！通过该路径未能找到要替换参数！{}", path);
                    return;
                }
                map = ((Map<String, Object>) map).get(ss[0]);
                int j = Integer.valueOf(ss[1]);
                if (map instanceof List) {
                    if (((List) map).size() > j) {
                        //path已经到末尾
                        if (i == ps.length - 1) {
                            ((List) map).set(j, value);
                        } else {
                            map = ((List) map).get(j);
                        }
                    } else {
                        //添加到列表末尾
                        if (i == ps.length - 1) {
                            ((List) map).add(value);
                        }
                    }

                } else {
                    logger.error("设置参数失败！通过该路径未能找到要替换参数！{}", path);
                    return;
                }
            } else {
                if (!((Map<String, Object>) map).containsKey(key)) {
                    ((Map<String, Object>) map).put(key, new HashMap<String, Object>());
                    //logger.error("设置参数失败！通过该路径未能找到要替换参数！{}", path);
                    //return;
                }
                //path已经到末尾
                if (i == ps.length - 1) {
                    ((Map<String, Object>) map).put(key, value);
                } else {
                    map = ((Map<String, Object>) map).get(key);
                }
            }
        }
    }

    public Request setParameters(Map<String, Object> parameters) {
        if (this.method.equals("get") || this.method.equals("delete")) {
            for (String key : parameters.keySet()) {
                this.path = replaceOrAdd(this.path, key, parameters.get(key).toString());
            }
        } else {
            if (this.parameter.size() == 0 || (!this.requestConfig.getParameters().trim().startsWith("{"))) {
                this.parameter.putAll(parameters);
            } else {
                mapCrawl(this.parameter, parameters);
            }
        }
        //logger.info(this.parameter.toString());
        return this;
    }

    private void mapCrawl(Map<String, Object> target, Map<String, Object> replacement) {
        Set<String> keySet = replacement.keySet();
        //json path 路径的设置
        for (String key : keySet) {
            if (key.startsWith("$.")) {
                this.setParameterByPath(key, replacement.get(key));
                replacement.remove(key);
            }
        }
        for (String key : target.keySet()) {
            if (keySet.contains(key)) {
                target.put(key, replacement.get(key));
            }
            Object value = target.get(key);
            if (value != null) {
                if (value instanceof java.util.List) {
                    for (Object o : (List) value) {
                        if (o instanceof java.util.Map) {
                            mapCrawl((Map<String, Object>) o, replacement);
                        }
                    }
                } else if (value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    Object[] os = new Object[length];
                    for (int i = 0; i < length; i++) {
                        Object o = Array.get(value, i);
                        if (o instanceof java.util.Map) {
                            mapCrawl((Map<String, Object>) o, replacement);
                        }
                    }
                } else if (value instanceof java.util.Map) {
                    mapCrawl((Map<String, Object>) value, replacement);
                }
            }
        }
    }

    private static void modify(JsonNode root, Map<String, Object> map) {
        Set<String> keySet = map.keySet();
        if (root.isObject()) {
            Iterator<String> it = root.fieldNames();
            while (it.hasNext()) {
                String key = it.next();
                if (keySet.contains(key)) {
                    Object value = map.get(key);
                    ObjectNode objectNode = (ObjectNode) root;
                    ObjectMapper mapper = new ObjectMapper();
                    if (value instanceof java.util.List) {
                        ArrayNode arrayNode = mapper.createArrayNode();
                        for (Object o : (List) value) {
                            writeValue(o, arrayNode);
                        }
                        ((ObjectNode) root).set(key, arrayNode);
                    } else if (value.getClass().isArray()) {
                        int length = Array.getLength(value);
                        Object[] os = new Object[length];
                        ArrayNode arrayNode = mapper.createArrayNode();
                        for (int i = 0; i < length; i++) {
                            writeValue(Array.get(value, i), arrayNode);
                        }
                        ((ObjectNode) root).set(key, arrayNode);
                    }
                } else {
                    modify(root.findValue(key), map);
                }
            }
        }
        if (root.isArray()) {
            Iterator<JsonNode> it = root.iterator();
            while (it.hasNext()) {
                modify(it.next(), map);
            }
        }
    }

    private static void jsonLeaf(JsonNode node, String name, Object value) throws IOException {
        if (node.isValueNode()) {
            //System.out.println(node.toString());
            return;
        }
        if (node.isObject()) {
            Iterator<String> it = node.fieldNames();
            //Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                //Map.Entry<String, JsonNode> entry = it.next();
                String key = it.next();
                if (key.equals(name)) {
                    ObjectNode objectNode = (ObjectNode) node;
                    //objectNode.put(key,value.toString());
                    ObjectMapper mapper = new ObjectMapper();

                    if (value instanceof java.util.List) {
                        ArrayNode arrayNode = mapper.createArrayNode();
                        for (Object o : (List) value) {
                            writeValue(o, arrayNode);
                        }
                        ((ObjectNode) node).set(key, arrayNode);
                    } else if (value.getClass().isArray()) {
                        int length = Array.getLength(value);
                        Object[] os = new Object[length];
                        ArrayNode arrayNode = mapper.createArrayNode();
                        for (int i = 0; i < length; i++) {
                            writeValue(Array.get(value, i), arrayNode);
                            //System.out.println(Array.get(value, i));
                            //System.out.println(Array.get(value, i).getClass());
                            //((ArrayNode)arrayNode).add(Array.get(value, i));
                        }
                        ((ObjectNode) node).set(key, arrayNode);
                    }

                } else {
                    jsonLeaf(node.findValue(key), name, value);
                }
            }
        }
        if (node.isArray()) {
            Iterator<JsonNode> it = node.iterator();
            while (it.hasNext()) {
                jsonLeaf(it.next(), name, value);
            }
        }
    }

    public static String replaceOrAdd(String url, String name, String value) {
        if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(value)) {
            if (url.contains(name + "=")) {
                url = url.replaceAll("(&" + name + "=[^&]*)", "&" + name + "=" + value);
                url = url.replaceAll("(\\?" + name + "=[^&]*)", "?" + name + "=" + value);
            } else {
                if (!url.contains("?")) {
                    url = url + "?";
                } else {
                    url = url + "&";
                }
                url = url + name + "=" + value;
            }
        }
        return url;
    }

    private boolean isTestMethodCalled() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        Class c = null;
        StackTraceElement ste = sts[3];
        //System.out.println(ste.getClassName() + "." + ste.getMethodName());
        try {
            c = Class.forName(ste.getClassName());
            for (Method method : c.getMethods()) {
                if (method.getName().equals(ste.getMethodName())) {
                    Test t = method.getAnnotation(Test.class);
                    if (t != null && c.getSuperclass().equals(AbstractTestNGSpringContextTests.class)) {
                        return true;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper MAPPER = new ObjectMapper();
        String s = "{'code':'0','desc':'查询保证金记录成功','data':{'dataSnapshot':{'totalAmount':109279050,'rechargeAmount':0,'deductAmount':0,'refundAmount':0},'list':[],'totalCount':0}}";
        //System.out.println(MAPPER.readValue(s.replace("'","\""), HashMap.class));
        JsonNode root = MAPPER.readTree(s.replace("'", "\""));
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("123");
        Object o = new Object();
        jsonLeaf(root, "totalAmount", list);
        logger.info(root.toString());
        System.out.println(root);
        String s1 = "http://manager.tuia.cn/homePage/queryAdvertData?pageSize=15&currentPage=1&startDate=2020-05-26&endDate=2020-05-26";
        s1 = replaceOrAdd(s1, "currentPage", "999");
        System.out.println(s1);
        Map<String, String> map = new HashMap<>();
        String s2 = "pageSize=50&currentPage=1&name=&checkStatus=&advertId=&companyName=&aeName=&sellName=&tradeId=&tableHeight=0&otherheight=12";
        String[] ss = s2.split("[=,&]");
        int len = ss.length;
        if ((len & 0x01) == 1) {
            len--;
        }
        for (int i = 0; i < len; i = i + 2) {
            map.put(ss[i], ss[i + 1]);
        }
        Map<String, String> map1 = new HashMap<>();
        map1.put("a", "1");
        map1.put("pageSize", "49");
        System.out.println(map);
        map.putAll(map1);
        System.out.println(map);
        String s3 = "book[1]";
        String[] ss1 = s3.split("[\\[ , \\]]");
        System.out.println(ss1);
        String url = "https://m.7hotest.com/item/detail3?id=731&pid=14";
        url = url.replaceAll("(&" + "pid" + "=[^&]*)", "&" + "pid" + "=" + 333);
        url = url.replaceAll("(\\?" + "id" + "=[^&]*)", "?" + "id" + "=" + 444);
        //url = url.replaceAll("(\\?"+"id" + "=[^&]*)", "\\?"+"id" + "=" + 333).replace("(\\&"+"pid" + "=[^&]*)", "pid" + "=" + 444);
        System.out.println(url);
        String s4 = "$abcde";
        System.out.println(s4.substring(1, s4.length()));
    }
}
