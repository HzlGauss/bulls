package com.bulls.qa;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.testng.TestNG;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"com.bulls.qa.*"})
public class BullsApplication {

    public static void main(String[] args) throws Exception {

        boolean sendNotice = false;
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (StringUtils.isNotEmpty(arg) && arg.equalsIgnoreCase("sendNotice=true")) {
                    System.out.println("=========sendNotice=true========");
                    System.setProperty("sendNotice", "true");
                }
            }
        }
        TestNG testNG = new TestNG();
        testNG.setUseDefaultListeners(false);
        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites = (List<XmlSuite>) new Parser(BullsApplication.class.getClassLoader().getResourceAsStream("testng-case.xml")).parse();
        testNG.setXmlSuites(suites);
        testNG.run();
    }
}
