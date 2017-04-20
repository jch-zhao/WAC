package com.wsddata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

@Controller
@EnableAutoConfiguration
public class ACMain {

	private Jedis jedis;
	
	@Value("${redis.host}")
	private String rhost;

	@Value("${redis.port}")
	private int rport;
	
	@RequestMapping("/")
    @ResponseBody
    String home() {
		jedis=new Jedis(rhost,rport);
		jedis.set("user", "{'id':'1','username:lisi','organization:calis'}");
		String value = jedis.get("user");
		
        return value;
    }
	
	public static void main(String[] args) throws Exception {
        SpringApplication.run(ACMain.class, args);
    }
}
