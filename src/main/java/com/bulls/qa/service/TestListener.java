package com.bulls.qa.service;

import com.bulls.qa.util.Postman;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestListener implements IReporter {
    int totalFailedTests = 0;
    int totalPassedTests = 0;
    int totalSkippedTests = 0;

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

        for (ISuite iSuite : suites) {
            Map<String, ISuiteResult> results = iSuite.getResults();
            for (ISuiteResult result : results.values()) {
                /*int failedTests = 0;
                int passedTests = 0;
                int skippedTests = 0;*/
                ITestContext context = result.getTestContext();
                //failedTests += context.getFailedTests().size();
                totalFailedTests += context.getFailedTests().size();
                //passedTests += context.getPassedTests().size();
                totalPassedTests += context.getPassedTests().size();
                //skippedTests += context.getSkippedTests().size();
                totalSkippedTests += context.getSkippedTests().size();
            }
        }
        System.out.println("+++++++++++++++++++");
        System.out.println(String.format("pass:%s,failed:%s,skipped:%s", totalPassedTests, totalFailedTests, totalSkippedTests));
        System.out.println("+++++++++++++++++++");
        Map<String, Object> result = new HashMap<>();
        result.put("total", totalPassedTests + totalFailedTests + totalSkippedTests);
        result.put("failCount", totalFailedTests);
        result.put("skipCount", totalSkippedTests);
        String resultUrl = "http://10.172.58.199/view/tuia_test/job/tuia_qiho_interface/allure/";
        result.put("resultUrl", resultUrl);
        result.put("project","tuiabq");
        if (Boolean.parseBoolean(System.getProperty("sendNotice"))) {
            Postman.send2Dingding(result);
        }

    }
}
