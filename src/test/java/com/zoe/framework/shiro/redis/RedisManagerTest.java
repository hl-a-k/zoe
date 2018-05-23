package com.zoe.framework.shiro.redis;

import org.junit.Test;

public class RedisManagerTest {
	
	
	@Test
	public void testSet(){
		RedisManager redisManager  = new RedisManager();
		redisManager.setHost("127.0.0.1");
		redisManager.setPort(6379);
		redisManager.setExpire(20);
		redisManager.setTimeout(0);
		redisManager.init();
		
		
		String key = "abc";
		UserMock u = new UserMock();
		u.setId("123");
		u.setLocked(true);
		u.setPassword("111");
		u.setSalt("222");
		u.setUsername("jack");
		
		redisManager.set(key.getBytes(), SerializeUtils.serialize(u));
	}


}
