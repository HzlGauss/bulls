package com.bulls.qa.testcase.testerhome;

import com.bulls.qa.configuration.SConfig;
import com.bulls.qa.request.Request;
import com.bulls.qa.service.CustomListener;
import com.bulls.qa.util.ExcelUtils;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author hzl
 * @date 2023/10/25
 */
@Slf4j
@ContextConfiguration(classes = SConfig.class)
//@Listeners(CustomListener.class)
public class Demo extends AbstractTestNGSpringContextTests {

    @Test(enabled = true, description = "打开帖子详情页→点赞")
    public void test() {
        log.info("test start");
        //请求实例1
        Request request = Request.getInstance("topics");
        //请求1发送
        Response response = request.doRequest();
        String html = response.asString();
        Headers  headers = response.getHeaders();
        Map<String, String> cookies = response.getCookies();
        Document document = Jsoup.parse(html);
        Element metaElement = document.select("meta[name=csrf-token]").first();
        String x_csrf_token = null;
        if (metaElement != null) {
            x_csrf_token = metaElement.attr("content");
        }
        request = Request.getInstance("likes");

        //request.addHeaders(headers);
        request.addCookies(cookies);
        if (x_csrf_token != null) {
            request.addHeader("x-csrf-token",x_csrf_token);
        }
        response = request.doRequest();
        assertThat(response.getStatusCode()).isGreaterThanOrEqualTo(200).as("返回状态码校验");
    }

    //参数化实例1
    @Test(enabled = true, description = "参数化demo,方法", dataProvider = "idList")
    public void dataProviderTest(String id, int type) {
        log.info("id:{},type:{}",id,type);
        assertThat(1+1>2).isTrue().as("断言失败举例");
    }

    //参数化实例2
    @Test(enabled = true, description = "参数化demo,excel", dataProvider = "ids")
    public void dataProviderTest2(String id, String type) {
        log.info("id:{},type:{}",id,type);
        assertThat(1+1==2).isTrue().as("断言举例");
    }

    @DataProvider(name = "idList")
    private Object[][] idList() {
        return new Object[][]{
                {"806a1d02410e1c4b6cbf05a85c383381", 1},
                {"806a1d02410e1c4b6cbf05a85c383382", 1}
        };
    }

    @DataProvider(name = "ids")
    private Object[][] excel() {
        File file = new File("data/types.xlsx");
        List<Map<Integer, String>> maps = ExcelUtils.readExcel(file);
        Object[][] datas = new Object[maps.get(0).size()][maps.size()];
        for (int j = 0; j < maps.size(); j++) {
            Map<Integer, String> map = maps.get(j);
            for (int i = 0; i < map.size(); i++) {
                datas[j][i] = map.get(i);
            }
        }
        return datas;
    }
}
