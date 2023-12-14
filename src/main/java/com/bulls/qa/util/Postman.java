package com.bulls.qa.util;

import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Created by hanzhanli on 2017/11/9.
 */
public class Postman {

    static String dingdingUrl = "https://shark.dui88.com/test/integration";
    static String dingdingPath = "https://oapi.dingtalk.com/robot/send?access_token=";
    static String master_dingdingUrl = "https://oapi.dingtalk.com/robot/send?access_token=f157794465b29d8803a898f2c97f603f73ca16499c2e5e3ffbe48ad19b85aed0";
    static String test_dingdingUrl = "https://oapi.dingtalk.com/robot/send?access_token=064650d21c6c722edddf8fd57616914ae4af0b369d3612061768906fc43b4fbb";
    static String advert_dingdingUrl = "https://oapi.dingtalk.com/robot/send?access_token=064650d21c6c722edddf8fd57616914ae4af0b369d3612061768906fc43b4fbb";
    static String ssp_dingdingUrl = "https://oapi.dingtalk.com/robot/send?access_token=064650d21c6c722edddf8fd57616914ae4af0b369d3612061768906fc43b4fbb";
    static String activity_dingdingUrl = "https://oapi.dingtalk.com/robot/send?access_token=064650d21c6c722edddf8fd57616914ae4af0b369d3612061768906fc43b4fbb";
    static String updateAutotestResultUrl = " https://console.dui88.com/pipeline/forward/at/updateAutotestResult";

    //                                        https://console.dui88.com/pipeline/forward/at/updateAutotestResult?branch=feature/202006121038_test2&status=1&msg=11
    public static void sendResult2Miria(Map result) {

        String total = result.get("total").toString();
        String fail = result.get("failCount").toString();
        String skip = result.get("skipCount").toString();
        String status = "";
        if (fail.equals("0") && skip.equals("0")) {
            status = "1";
        } else {
            status = "-1";
        }
        String branch = System.getProperty("testBranch");
        String msg = result.get("resultUrl").toString();

        Map map = new HashMap();
        map.put("status", status);
        map.put("branch", branch);
        map.put("msg", msg);

        Response response = given().params(map).get(updateAutotestResultUrl);

    }

    public static void send2Dingding(Map result) {

        String project = (String) result.get("project");
        //boolean isTestMaster = Boolean.parseBoolean(System.getProperty("isTestMaster"));
        String dingding_host = dingdingPath;
        //String token = System.getProperty("token");
        dingding_host = master_dingdingUrl;
        String content = getContentByMap(result);
        sendDingding(content, "", dingding_host);

    }

    public static String getContentByMap(Map result) {
        //String testBranch = System.getProperty("testBranch");
        String content = "";
        try {
            content += "自动化测试反馈&值班提醒\n\n";
            content += "测试模块： " + result.get("project").toString() + "\n";
            content += "测试分支：master\n";
            if (result.containsKey("exceptionMessage")) {
                content += "失败原因： " + result.get("exceptionMessage") + "\n";
            }
            content += "  用例总数：" + result.get("total").toString() + "\n";
            content += "  发现问题总数：" + result.get("failCount").toString() + "\n";
            content += "  未执行脚本总数：" + result.get("skipCount").toString() + "\n";

            content += "  报告详情：" + result.get("resultUrl").toString() + "";
        } catch (Exception e) {
            e.getMessage();
            content += "--------------------内容组装异常-----------------";
        }

        return content;
    }


    public static void sendDingding(String content, String mobile, String dingding_host) {

        Map<String, Object> map = new HashMap<>();

        map.put("msgtype", "text");
        Map<String, Object> text = new HashMap<>();
        text.put("content", content);
        map.put("text", text);
        Map<String, Object> at = new HashMap<>();
        List<String> atmobiles = new ArrayList();
        atmobiles.add(mobile);
        at.put("atMobiles", atmobiles);
        at.put("isAtAll", false);
        map.put("at", at);

        Map<String, Object> headermap = new HashMap<>();
        headermap.put("Content-Type", "application/json");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(content);
        System.out.println(dingding_host);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        Response response = given().headers(headermap).body(map).post(dingding_host);

    }

    public static void main(String[] args) {

        String content = "值班提醒test";
        String token = System.getProperty("token");
        System.out.println(token);
        System.out.println(token == null);

    }

}