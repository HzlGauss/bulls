package com.bulls.qa.service;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import lombok.extern.slf4j.Slf4j;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.util.List;

@Slf4j
public class CustomListener extends TestListenerAdapter implements ITestListener {
    private ExtentReports extent;
    private ExtentTest test;


    @Override
    public void onStart(ITestContext context) {
        extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter("bulls.html");
        extent.attachReporter(spark);
    }

    @Override
    public void onTestStart(ITestResult result) {
        test = extent.createTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
        //log.info("getName:{},getParameters:{},getAttributeNames:{}", result.getName(), result.getParameters(), result.getAttributeNames());
        //log.info("testContext:{}", result.getTestContext().getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.log(Status.PASS, "Test Passed");
        List<String> outputs = Reporter.getOutput(result);
        for (String output : outputs) {
            test.info(output);
        }
        //test.info(Reporter.getOutput(result)+"");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.fail("Test Failed");
        test.log(Status.FAIL, result.getThrowable());
        List<String> outputs = Reporter.getOutput(result);
        for (String output : outputs) {
            test.info(output);
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
