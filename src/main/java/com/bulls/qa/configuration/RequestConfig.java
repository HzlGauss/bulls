package com.bulls.qa.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@Data
public class RequestConfig {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String path;
    @Getter
    @Setter
    private String headers;
    @Getter
    @Setter
    private String parameters;
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private String method;

    @Getter
    @Setter
    private String cookies;

    private String filePath;

    public void setParameters(String parameters) {
        if (StringUtils.isNotEmpty(parameters.trim())) {
            if(parameters.startsWith("r\\")){
                this.parameters=parameters.substring(2, parameters.length());
            }else {
                this.parameters = parameters.trim().replace("\\", "");
            }

        }

    }
}
