package com.bulls.qa.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Data
public class PioneerConfig extends RequestConfig {
    @Setter
    @Getter
    int priority;



    private List<Map<String, String>> requiredParameter;
    @Getter
    private List<Map<String, String>> extractors;

    public void setRequiredParameter(String requiredParameter) {
        String s = requiredParameter.trim().replace("\\", "");
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.requiredParameter = mapper.readValue(s, new TypeReference<List<Map<String, Object>>>() {
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setExtractors(String extractors) {
        String s = extractors.trim().replace("\\", "");
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.extractors = mapper.readValue(s, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
