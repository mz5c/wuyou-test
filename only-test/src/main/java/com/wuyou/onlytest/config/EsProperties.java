package com.wuyou.onlytest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "es")
public class EsProperties {

    private String endpoint = "http://localhost:9200";
    private String username = "elastic";
    private String password = "";
}
