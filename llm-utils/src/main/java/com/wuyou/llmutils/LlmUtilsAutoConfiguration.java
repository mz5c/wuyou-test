package com.wuyou.llmutils;

import com.wuyou.llmutils.client.LlmClient;
import com.wuyou.llmutils.properties.LlmProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnClass({RestTemplate.class})
@ConditionalOnProperty(prefix = "llm.api", name = "api-key")
@EnableConfigurationProperties(LlmProperties.class)
@AutoConfigureAfter(RestTemplateAutoConfiguration.class)
public class LlmUtilsAutoConfiguration {

    @Bean
    public LlmClient llmClient(LlmProperties properties, RestTemplateBuilder builder) {
        return new LlmClient(properties, builder);
    }
}
