package com.wuyou.onlytest.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisDataStructureService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LIST_KEY = "demo:list";
    private static final String HASH_KEY = "demo:hash";
    private static final String SET_KEY = "demo:set";
    private static final String ZSET_KEY = "demo:zset";

    // ==================== List 操作 ====================

    public Long listPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public Long listLeftPush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public Object listRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    public Object listLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public List<Object> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public Long listSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public void listSet(String key, long index, String value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    public Long listRemove(String key, long count, String value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    // ==================== Hash 操作 ====================

    public void hashPut(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public Object hashGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public Map<Object, Object> hashEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Set<Object> hashKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    public List<Object> hashValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    public Boolean hashHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    public Long hashDelete(String key, String... fields) {
        return redisTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    public Long hashSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    public void hashPutAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    // ==================== Set 操作 ====================

    public Long setAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, (Object[]) values);
    }

    public Long setRemove(String key, String... values) {
        return redisTemplate.opsForSet().remove(key, (Object[]) values);
    }

    public Set<Object> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Boolean setIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long setSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public Set<Object> setIntersect(String key1, String key2) {
        return redisTemplate.opsForSet().intersect(key1, key2);
    }

    public Set<Object> setUnion(String key1, String key2) {
        return redisTemplate.opsForSet().union(key1, key2);
    }

    public Set<Object> setDifference(String key1, String key2) {
        return redisTemplate.opsForSet().difference(key1, key2);
    }

    public Object setRandomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    public Object setPop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    // ==================== ZSet 操作 ====================

    public Boolean zsetAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    public Long zsetAddBatch(String key, Map<String, Double> members) {
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();
        members.forEach((v, s) ->
                tuples.add(new org.springframework.data.redis.core.DefaultTypedTuple<>(v, s)));
        return redisTemplate.opsForZSet().add(key, tuples);
    }

    public Double zsetScore(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    public Long zsetRank(String key, String value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    public Long zsetReverseRank(String key, String value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    public Set<Object> zsetRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public Set<Object> zsetReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    public Set<Object> zsetRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    public Long zsetSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    public Long zsetCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    public Double zsetIncrementScore(String key, String value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    public Long zsetRemove(String key, String... values) {
        return redisTemplate.opsForZSet().remove(key, (Object[]) values);
    }

    public void zsetRemoveRange(String key, long start, long end) {
        redisTemplate.opsForZSet().removeRange(key, start, end);
    }
}
