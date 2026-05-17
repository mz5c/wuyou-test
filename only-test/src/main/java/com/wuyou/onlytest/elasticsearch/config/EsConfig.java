package com.wuyou.onlytest.elasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuyou.onlytest.config.EsProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Configuration
public class EsConfig {

    @Bean
    public RestTemplate esRestTemplate(EsProperties esProperties) {
        URI uri = URI.create(esProperties.getEndpoint());
        return new RestTemplateBuilder()
                .rootUri(uri.toString())
                .basicAuthentication(esProperties.getUsername(), esProperties.getPassword())
                .build();
    }

    @Bean(destroyMethod = "close")
    public org.elasticsearch.client.RestClient esLowLevelClient(EsProperties esProperties) {
        URI uri = URI.create(esProperties.getEndpoint());
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(esProperties.getUsername(), esProperties.getPassword()));

        return org.elasticsearch.client.RestClient.builder(
                        new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
    }

    @Bean
    public JacksonJsonpMapper jacksonJsonpMapper(ObjectMapper objectMapper) {
        return new JacksonJsonpMapper(objectMapper);
    }

    @Bean(destroyMethod = "close")
    public RestClientTransport esTransport(
            org.elasticsearch.client.RestClient esLowLevelClient,
            JacksonJsonpMapper jsonpMapper) {
        return new RestClientTransport(esLowLevelClient, jsonpMapper);
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClientTransport esTransport) {
        return new ElasticsearchClient(esTransport);
    }
}
