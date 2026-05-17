package com.wuyou.onlytest.elasticsearch.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.elasticsearch.service.EsHttpApiService;
import com.wuyou.onlytest.elasticsearch.service.EsSpringDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "ES 操作测试")
@RestController
@RequestMapping("/api/v1/es")
@RequiredArgsConstructor
public class EsController {

    private final EsHttpApiService esHttpApiService;
    private final EsSpringDataService esSpringDataService;

    // ========================================================================
    // 方式一：HTTP API（无 ES 依赖，使用 RestTemplate）
    // ========================================================================

    @Tag(name = "ES HTTP API")
    @RestController
    @RequestMapping("/api/v1/es/http")
    @RequiredArgsConstructor
    public static class EsHttpApiController {

        private final EsHttpApiService esHttpApiService;

        // ---------- 索引管理 ----------

        @Operation(summary = "创建索引")
        @PostMapping("/index/{name}")
        public Result<Map<String, Object>> createIndex(
                @PathVariable String name,
                @RequestBody(required = false) Map<String, Object> body) {
            Map<String, Object> settings = null;
            Map<String, Object> mappings = null;
            if (body != null) {
                Object s = body.get("settings");
                Object m = body.get("mappings");
                if (s instanceof Map) settings = (Map<String, Object>) s;
                if (m instanceof Map) mappings = (Map<String, Object>) m;
            }
            return Result.success(esHttpApiService.createIndex(name, settings, mappings));
        }

        @Operation(summary = "检查索引是否存在")
        @GetMapping("/index/{name}/exists")
        public Result<Boolean> indexExists(@PathVariable String name) {
            return Result.success(esHttpApiService.indexExists(name));
        }

        @Operation(summary = "获取索引信息")
        @GetMapping("/index/{name}")
        public Result<Map<String, Object>> getIndex(@PathVariable String name) {
            return Result.success(esHttpApiService.getIndex(name));
        }

        @Operation(summary = "删除索引")
        @DeleteMapping("/index/{name}")
        public Result<Map<String, Object>> deleteIndex(@PathVariable String name) {
            return Result.success(esHttpApiService.deleteIndex(name));
        }

        @Operation(summary = "列出所有索引")
        @GetMapping("/indices")
        public Result<List<Map<String, Object>>> listIndices() {
            return Result.success(esHttpApiService.listIndices());
        }

        // ---------- 文档操作 ----------

        @Operation(summary = "创建文档（自动生成 ID）")
        @PostMapping("/doc/{index}")
        public Result<Map<String, Object>> createDoc(
                @PathVariable String index,
                @RequestBody Map<String, Object> doc) {
            return Result.success(esHttpApiService.createDocument(index, doc));
        }

        @Operation(summary = "创建/更新文档（指定 ID）")
        @PutMapping("/doc/{index}/{id}")
        public Result<Map<String, Object>> indexDoc(
                @PathVariable String index,
                @PathVariable String id,
                @RequestBody Map<String, Object> doc) {
            return Result.success(esHttpApiService.indexDocument(index, id, doc));
        }

        @Operation(summary = "获取文档")
        @GetMapping("/doc/{index}/{id}")
        public Result<Map<String, Object>> getDoc(
                @PathVariable String index,
                @PathVariable String id) {
            return Result.success(esHttpApiService.getDocument(index, id));
        }

        @Operation(summary = "更新文档（部分更新）")
        @PostMapping("/doc/{index}/{id}/update")
        public Result<Map<String, Object>> updateDoc(
                @PathVariable String index,
                @PathVariable String id,
                @RequestBody Map<String, Object> doc) {
            return Result.success(esHttpApiService.updateDocument(index, id, doc));
        }

        @Operation(summary = "删除文档")
        @DeleteMapping("/doc/{index}/{id}")
        public Result<Map<String, Object>> deleteDoc(
                @PathVariable String index,
                @PathVariable String id) {
            return Result.success(esHttpApiService.deleteDocument(index, id));
        }

        @Operation(summary = "搜索文档")
        @PostMapping("/search/{index}")
        public Result<Map<String, Object>> search(
                @PathVariable String index,
                @RequestBody(required = false) Map<String, Object> query) {
            return Result.success(esHttpApiService.search(index, query));
        }
    }

    // ========================================================================
    // 方式二：Spring Data Elasticsearch（引入 ES 依赖）
    // ========================================================================

    @Tag(name = "ES Spring Data")
    @RestController
    @RequestMapping("/api/v1/es/sd")
    @RequiredArgsConstructor
    public static class EsSpringDataController {

        private final EsSpringDataService esSpringDataService;

        // ---------- 索引管理 ----------

        @Operation(summary = "创建索引")
        @PostMapping("/index/{name}")
        public Result<Map<String, Object>> createIndex(
                @PathVariable String name,
                @RequestBody(required = false) Map<String, Object> body) {
            Map<String, Object> settings = null;
            Map<String, Object> mappings = null;
            if (body != null) {
                Object s = body.get("settings");
                Object m = body.get("mappings");
                if (s instanceof Map) settings = (Map<String, Object>) s;
                if (m instanceof Map) mappings = (Map<String, Object>) m;
            }
            return Result.success(esSpringDataService.createIndex(name, settings, mappings));
        }

        @Operation(summary = "检查索引是否存在")
        @GetMapping("/index/{name}/exists")
        public Result<Boolean> indexExists(@PathVariable String name) {
            return Result.success(esSpringDataService.indexExists(name));
        }

        @Operation(summary = "获取索引信息")
        @GetMapping("/index/{name}")
        public Result<Map<String, Object>> getIndex(@PathVariable String name) {
            return Result.success(esSpringDataService.getIndex(name));
        }

        @Operation(summary = "删除索引")
        @DeleteMapping("/index/{name}")
        public Result<Boolean> deleteIndex(@PathVariable String name) {
            return Result.success(esSpringDataService.deleteIndex(name));
        }

        // ---------- 文档操作 ----------

        @Operation(summary = "创建文档（自动生成 ID）")
        @PostMapping("/doc/{index}")
        public Result<Map<String, Object>> createDoc(
                @PathVariable String index,
                @RequestBody Map<String, Object> doc) {
            return Result.success(esSpringDataService.createDocument(index, doc));
        }

        @Operation(summary = "创建/更新文档（指定 ID）")
        @PutMapping("/doc/{index}/{id}")
        public Result<Map<String, Object>> indexDoc(
                @PathVariable String index,
                @PathVariable String id,
                @RequestBody Map<String, Object> doc) {
            return Result.success(esSpringDataService.indexDocument(index, id, doc));
        }

        @Operation(summary = "获取文档")
        @GetMapping("/doc/{index}/{id}")
        public Result<Map<String, Object>> getDoc(
                @PathVariable String index,
                @PathVariable String id) {
            return Result.success(esSpringDataService.getDocument(index, id));
        }

        @Operation(summary = "更新文档（部分更新）")
        @PostMapping("/doc/{index}/{id}/update")
        public Result<Map<String, Object>> updateDoc(
                @PathVariable String index,
                @PathVariable String id,
                @RequestBody Map<String, Object> doc) {
            return Result.success(esSpringDataService.updateDocument(index, id, doc));
        }

        @Operation(summary = "删除文档")
        @DeleteMapping("/doc/{index}/{id}")
        public Result<Map<String, Object>> deleteDoc(
                @PathVariable String index,
                @PathVariable String id) {
            return Result.success(esSpringDataService.deleteDocument(index, id));
        }

        // ---------- 搜索 ----------

        @Operation(summary = "全量搜索")
        @GetMapping("/search/{index}")
        public Result<Map<String, Object>> searchAll(
                @PathVariable String index,
                @RequestParam(defaultValue = "10") int size) {
            return Result.success(esSpringDataService.searchAll(index, size));
        }

        @Operation(summary = "按字段搜索")
        @GetMapping("/search/{index}/field")
        public Result<Map<String, Object>> searchByField(
                @PathVariable String index,
                @RequestParam String field,
                @RequestParam String value,
                @RequestParam(defaultValue = "10") int size) {
            return Result.success(esSpringDataService.searchByField(index, field, value, size));
        }

        @Operation(summary = "原始 JSON 查询搜索")
        @PostMapping("/search/{index}/raw")
        public Result<Map<String, Object>> searchRaw(
                @PathVariable String index,
                @RequestBody Map<String, Object> query,
                @RequestParam(defaultValue = "10") int size) {
            return Result.success(esSpringDataService.searchWithRawQuery(index, query, size));
        }
    }
}
