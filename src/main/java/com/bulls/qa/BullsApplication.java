package com.bulls.qa;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.testng.TestNG;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"com.bulls.qa.*"})
public class BullsApplication {

    public static void main(String[] args) throws Exception {
        InputStream casesXmlPath=BullsApplication.class.getClassLoader().getResourceAsStream("testng-case.xml");
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (StringUtils.isNotEmpty(arg) && arg.endsWith(".xml")) {
                    casesXmlPath=new FileInputStream(arg);
                }
            }
        }
        TestNG testNG = new TestNG();
        testNG.setUseDefaultListeners(false);
        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites = (List<XmlSuite>) new Parser(casesXmlPath).parse();
        //
        testNG.setXmlSuites(suites);
        testNG.run();
    }
}
