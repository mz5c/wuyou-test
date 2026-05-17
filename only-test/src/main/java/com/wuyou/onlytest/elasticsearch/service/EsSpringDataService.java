package com.wuyou.onlytest.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.stream.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsSpringDataService {

    private final ElasticsearchClient client;
    private final JsonpMapper jsonpMapper;
    private final ObjectMapper objectMapper;

    // ==================== 索引管理 ====================

    /** 创建索引 */
    public Map<String, Object> createIndex(String indexName, Map<String, Object> settings, Map<String, Object> mappings) {
        try {
            boolean exists = client.indices().exists(e -> e.index(indexName)).value();
            if (exists) {
                return Collections.singletonMap("result", "索引已存在: " + indexName);
            }

            CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder().index(indexName);

            if (settings != null && !settings.isEmpty()) {
                String json = toJson(Map.of("index", settings));
                try (JsonParser parser = jsonpMapper.jsonProvider().createParser(new StringReader(json))) {
                    builder.settings(s -> s.withJson(parser, jsonpMapper));
                }
            }
            if (mappings != null && !mappings.isEmpty()) {
                String json = toJson(mappings);
                try (JsonParser parser = jsonpMapper.jsonProvider().createParser(new StringReader(json))) {
                    builder.mappings(m -> m.withJson(parser, jsonpMapper));
                }
            }

            CreateIndexResponse response = client.indices().create(builder.build());
            return Map.of("acknowledged", response.acknowledged(), "index", response.index());
        } catch (IOException e) {
            throw new RuntimeException("创建索引失败: " + indexName, e);
        }
    }

    /** 检查索引是否存在 */
    public boolean indexExists(String indexName) {
        try {
            return client.indices().exists(e -> e.index(indexName)).value();
        } catch (IOException e) {
            throw new RuntimeException("检查索引失败: " + indexName, e);
        }
    }

    /** 获取索引信息 */
    public Map<String, Object> getIndex(String indexName) {
        try {
            boolean exists = client.indices().exists(e -> e.index(indexName)).value();
            if (!exists) {
                return Collections.singletonMap("error", "索引不存在: " + indexName);
            }

            GetIndexResponse response = client.indices().get(g -> g.index(indexName));
            IndexState state = response.get(indexName);

            return Map.of(
                    "settings", state.settings() != null ? toMap(state.settings()) : Collections.emptyMap(),
                    "mappings", state.mappings() != null ? toMap(state.mappings()) : Collections.emptyMap()
            );
        } catch (IOException e) {
            throw new RuntimeException("获取索引信息失败: " + indexName, e);
        }
    }

    /** 删除索引 */
    public boolean deleteIndex(String indexName) {
        try {
            DeleteIndexResponse response = client.indices().delete(d -> d.index(indexName));
            return response.acknowledged();
        } catch (IOException e) {
            throw new RuntimeException("删除索引失败: " + indexName, e);
        }
    }

    // ==================== 文档操作 ====================

    /** 创建文档（自动生成 ID） */
    public Map<String, Object> createDocument(String index, Map<String, Object> doc) {
        try {
            IndexResponse response = client.index(i -> i
                    .index(index)
                    .document(doc)
            );
            return Map.of(
                    "result", response.result().jsonValue(),
                    "_id", response.id(),
                    "_index", response.index()
            );
        } catch (IOException e) {
            throw new RuntimeException("创建文档失败: " + index, e);
        }
    }

    /** 创建/更新文档（指定 ID） */
    public Map<String, Object> indexDocument(String index, String id, Map<String, Object> doc) {
        try {
            IndexResponse response = client.index(i -> i
                    .index(index)
                    .id(id)
                    .document(doc)
            );
            return Map.of(
                    "result", response.result().jsonValue(),
                    "_id", response.id(),
                    "_index", response.index()
            );
        } catch (IOException e) {
            throw new RuntimeException("索引文档失败: " + index + "/" + id, e);
        }
    }

    /** 获取文档 */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDocument(String index, String id) {
        try {
            GetResponse<Map> response = client.get(g -> g
                            .index(index)
                            .id(id),
                    Map.class
            );
            if (!response.found()) {
                return Collections.singletonMap("found", false);
            }
            return Map.of("found", true, "_id", id, "_source", response.source());
        } catch (IOException e) {
            throw new RuntimeException("获取文档失败: " + index + "/" + id, e);
        }
    }

    /** 更新文档（部分更新） */
    public Map<String, Object> updateDocument(String index, String id, Map<String, Object> doc) {
        try {
            UpdateResponse<Map> response = client.update(u -> u
                            .index(index)
                            .id(id)
                            .doc(doc),
                    Map.class
            );
            return Map.of(
                    "result", response.result().jsonValue(),
                    "_id", response.id(),
                    "_index", response.index()
            );
        } catch (IOException e) {
            throw new RuntimeException("更新文档失败: " + index + "/" + id, e);
        }
    }

    /** 删除文档 */
    public Map<String, Object> deleteDocument(String index, String id) {
        try {
            DeleteResponse response = client.delete(d -> d
                    .index(index)
                    .id(id)
            );
            return Map.of(
                    "result", response.result().jsonValue(),
                    "_id", response.id(),
                    "_index", response.index()
            );
        } catch (IOException e) {
            // 文档不存在时 status 404 也正常返回
            return Map.of("result", "not_found", "_id", id, "_index", index);
        }
    }

    // ==================== 搜索 ====================

    /** 搜索（matchAll） */
    public Map<String, Object> searchAll(String index, int size) {
        try {
            SearchResponse<Map> response = client.search(s -> s
                            .index(index)
                            .query(q -> q.matchAll(t -> t))
                            .size(size),
                    Map.class
            );
            return buildSearchResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("搜索失败: " + index, e);
        }
    }

    /** 搜索（按字段匹配） */
    public Map<String, Object> searchByField(String index, String field, String value, int size) {
        try {
            SearchResponse<Map> response = client.search(s -> s
                            .index(index)
                            .query(q -> q.match(t -> t.field(field).query(value)))
                            .size(size),
                    Map.class
            );
            return buildSearchResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("搜索失败: " + index, e);
        }
    }

    /** 搜索（原始 JSON 查询） */
    public Map<String, Object> searchWithRawQuery(String index, Map<String, Object> rawQuery, int size) {
        try {
            String json = toJson(rawQuery);
            Query query;
            try (JsonParser parser = jsonpMapper.jsonProvider().createParser(new StringReader(json))) {
                query = Query._DESERIALIZER.deserialize(parser, jsonpMapper);
            }
            SearchResponse<Map> response = client.search(s -> s
                            .index(index)
                            .query(query)
                            .size(size),
                    Map.class
            );
            return buildSearchResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("搜索失败: " + index, e);
        }
    }

    // ==================== 辅助方法 ====================

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildSearchResponse(SearchResponse<Map> response) {
        if (response == null || response.hits() == null) {
            return Map.of("total", 0L, "hits", Collections.emptyList());
        }

        long total = response.hits().total() != null ? response.hits().total().value() : 0;
        double maxScore = response.hits().maxScore() != null ? response.hits().maxScore() : 0.0;

        List<Map<String, Object>> hits = response.hits().hits().stream()
                .map(hit -> {
                    Map<String, Object> hitMap = new LinkedHashMap<>();
                    hitMap.put("_id", hit.id());
                    hitMap.put("_score", hit.score());
                    hitMap.put("_source", hit.source());
                    return hitMap;
                })
                .collect(Collectors.toList());

        return Map.of(
                "total", total,
                "max_score", maxScore,
                "hits", hits
        );
    }

    /** 将 ES 响应对象序列化为 Map */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("序列化 ES 对象失败", e);
            return Collections.singletonMap("raw", obj.toString());
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }
}
