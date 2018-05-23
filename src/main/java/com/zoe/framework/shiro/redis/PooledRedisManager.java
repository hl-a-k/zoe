package com.zoe.framework.shiro.redis;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

/**
 * 使用JedisSentinelPool的RedisManager
 */
public class PooledRedisManager extends RedisManager {
    private JedisSentinelPool jedisSentinelPool;

    public PooledRedisManager() {

    }

    @Override
    protected Pool<Jedis> createJedisPool() {
        return getJedisSentinelPool();
    }

    /*增加 Redis Sentinel 高可用（HA）方案配置（Redis哨兵） */
    public JedisSentinelPool getJedisSentinelPool() {
        return this.jedisSentinelPool;
    }

    public void setJedisSentinelPool(JedisSentinelPool jedisSentinelPool) {
        this.jedisSentinelPool = jedisSentinelPool;
    }
}
