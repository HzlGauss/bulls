package com.bulls.qa.configuration;


import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;


@Configuration
@ConfigurationPropertiesScan(basePackages = {"com.bulls.qa.*"})
@ComponentScan(basePackages = {"com.bulls.qa.*"})
@PropertySource(value = {"application.properties", "${spring.profiles.active}/test.properties"},encoding="UTF-8")
public class SConfig {

}
