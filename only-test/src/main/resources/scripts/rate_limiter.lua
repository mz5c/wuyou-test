-- 滑动窗口限流
local key = KEYS[1]
local windowMs = tonumber(ARGV[1])
local maxRequests = tonumber(ARGV[2])
local now = redis.call('TIME')[1] * 1000

redis.call('ZREMRANGEBYSCORE', key, 0, now - windowMs)
local count = redis.call('ZCARD', key)
if count < maxRequests then
    redis.call('ZADD', key, now, now .. ':' .. math.random())
    redis.call('EXPIRE', key, math.ceil(windowMs / 1000))
    return 1
end
return 0
