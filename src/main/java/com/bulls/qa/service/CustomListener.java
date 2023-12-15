package com.bulls.qa.service;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.*;
import org.testng.xml.XmlSuite;

import java.util.List;

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
        test = extent.createTest(result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.log(Status.PASS, "Test Passed");
    }
    @Override
    public void onTestFailure(ITestResult result) {
        test.fail("Test Failed");
        test.log(Status.FAIL, result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
