package com.wsddata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import com.wsddata.Conf;

@Controller
@EnableAutoConfiguration
@ComponentScan
public class ACMain {
	private final Conf config;
	
	private Jedis jedis;
	
	@Autowired
	public ACMain(Conf conf){
		this.config=conf;
		jedis=new Jedis(config.redis_host,config.redis_port);
	}
	
	@RequestMapping("/")
    @ResponseBody
    String home() {
		jedis.set("user", "{'id':'1','username:lisi','organization:calis'}");
		String value = jedis.get("user");
		
        return value;
    }
	
	public static void main(String[] args) throws Exception {
        SpringApplication.run(ACMain.class, args);
    }
}
