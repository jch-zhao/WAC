package com.wsddata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Conf {
	@Value("${redis.host}")
	public String redisHost;
	
	@Value("${redis.port}")
	public int redisPort;
	
	@Value("${redis.max_total}")
	public int redisMaxTotal;
	
	@Value("${redis.max_idle}")
	public int redisMaxIdle;
	
	@Value("${redis.max_waitmillis}")
	public int redisMaxWaitmillis;
}
