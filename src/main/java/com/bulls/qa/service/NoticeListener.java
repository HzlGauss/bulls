package com.bulls.qa.service;

import groovy.util.logging.Slf4j;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class NoticeListener implements IReporter {
    int totalFailedTests = 0;
    int totalPassedTests = 0;
    int totalSkippedTests = 0;

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

        for (ISuite iSuite : suites) {
            Map<String, ISuiteResult> results = iSuite.getResults();
            for (ISuiteResult result : results.values()) {
                ITestContext context = result.getTestContext();

                totalFailedTests += context.getFailedTests().size();

                totalPassedTests += context.getPassedTests().size();

                totalSkippedTests += context.getSkippedTests().size();
            }
        }
        // TODO:send notice message
    }
}
