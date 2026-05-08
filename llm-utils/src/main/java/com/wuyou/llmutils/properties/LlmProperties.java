package com.wuyou.llmutils.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "llm.api")
public class LlmProperties {
    private String baseUrl = "https://api.openai.com";
    private String apiKey;
    private String model = "gpt-4o";
    private int timeout = 30000;
}
