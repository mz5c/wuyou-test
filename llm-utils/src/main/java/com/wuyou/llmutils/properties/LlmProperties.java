package com.wuyou.llmutils.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "llm.api")
public class LlmProperties {
    private String fullUrl = "https://api.openai.com/v1/chat/completions";
    private String apiKey;
    private String model = "gpt-4o";
    private int timeout = 30000;
}
