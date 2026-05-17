package com.wuyou.onlytest.elasticsearch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsHttpApiService {

    private final RestTemplate esRestTemplate;
    private final ObjectMapper objectMapper;

    // ==================== 索引管理 ====================

    /** 创建索引（带 settings 和 mappings） */
    public Map<String, Object> createIndex(String indexName, Map<String, Object> settings, Map<String, Object> mappings) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        if (settings != null && !settings.isEmpty()) {
            body.put("settings", settings);
        }
        if (mappings != null && !mappings.isEmpty()) {
            body.put("mappings", mappings);
        }
        return callEsApi("/" + indexName, HttpMethod.PUT, body.isEmpty() ? null : body);
    }

    /** 检查索引是否存在 */
    public boolean indexExists(String indexName) {
        try {
            esRestTemplate.exchange(
                    "/" + indexName,
                    HttpMethod.HEAD,
                    null,
                    Void.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 获取索引信息（settings + mappings） */
    public Map<String, Object> getIndex(String indexName) {
        return callEsApi("/" + indexName, HttpMethod.GET, null);
    }

    /** 删除索引 */
    public Map<String, Object> deleteIndex(String indexName) {
        return callEsApi("/" + indexName, HttpMethod.DELETE, null);
    }

    /** 列出所有索引（_cat API） */
    public List<Map<String, Object>> listIndices() {
        String response = esRestTemplate.getForObject(
                "/_cat/indices?format=json&bytes=b", String.class);
        try {
            return objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("解析 ES 索引列表失败", e);
            return Collections.emptyList();
        }
    }

    // ==================== 文档操作 ====================

    /** 创建文档（自动生成 ID） */
    public Map<String, Object> createDocument(String index, Map<String, Object> doc) {
        return callEsApi("/" + index + "/_doc", HttpMethod.POST, doc);
    }

    /** 创建/更新文档（指定 ID） */
    public Map<String, Object> indexDocument(String index, String id, Map<String, Object> doc) {
        return callEsApi("/" + index + "/_doc/" + id, HttpMethod.PUT, doc);
    }

    /** 获取文档 */
    public Map<String, Object> getDocument(String index, String id) {
        return callEsApi("/" + index + "/_doc/" + id, HttpMethod.GET, null);
    }

    /** 更新文档（部分更新） */
    public Map<String, Object> updateDocument(String index, String id, Map<String, Object> doc) {
        return callEsApi("/" + index + "/_update/" + id, HttpMethod.POST, Collections.singletonMap("doc", doc));
    }

    /** 删除文档 */
    public Map<String, Object> deleteDocument(String index, String id) {
        return callEsApi("/" + index + "/_doc/" + id, HttpMethod.DELETE, null);
    }

    /** 搜索文档 */
    public Map<String, Object> search(String index, Map<String, Object> query) {
        return callEsApi("/" + index + "/_search", HttpMethod.POST, query);
    }

    // ==================== 通用调用 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> callEsApi(String path, HttpMethod method, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = body != null
                ? new HttpEntity<>(body, headers)
                : new HttpEntity<>(headers);

        String response = esRestTemplate.exchange(path, method, entity, String.class).getBody();
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.error("解析 ES 响应失败: {}", response, e);
            return Collections.singletonMap("error", "解析响应失败: " + e.getMessage());
        }
    }
}
