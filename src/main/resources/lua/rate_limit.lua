-- Rate limiting Lua script for Redis
-- KEYS[1]: Redis key (e.g., "gateway:ratelimit:user123:2025-09-29T10:30")
-- ARGV[1]: Rate limit (e.g., 100)
-- ARGV[2]: Window in seconds (e.g., 60)

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

-- Increment counter
local current = redis.call('incr', key)

-- Set expiry on first request
if current == 1 then
    redis.call('expire', key, window)
end

-- Check if limit exceeded
if current > limit then
    return 0  -- Rate limit exceeded
else
    return limit - current  -- Remaining requests
end