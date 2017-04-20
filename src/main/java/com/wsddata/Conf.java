package com.wsddata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Conf {
	@Value("${redis.host}")
	public String redis_host;
	
	@Value("${redis.port}")
	public int redis_port;
	
}
