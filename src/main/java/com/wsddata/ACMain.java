package com.wsddata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.*;


@Controller
@EnableAutoConfiguration
@ComponentScan
public class ACMain {
	private Jedis jedis;
	private JedisPool jedisPool;

	@Autowired
	public ACMain(Conf conf){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(conf.redisMaxTotal);
		config.setMaxIdle(conf.redisMaxIdle);
		config.setMaxWaitMillis(conf.redisMaxWaitmillis);
		try{
			jedisPool=new JedisPool(config,conf.redisHost,conf.redisPort);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/login")
    @ResponseBody
    String login() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/logout")
    @ResponseBody
    String logout() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/registerApp")
    @ResponseBody
    String registerApp() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/registerUser")
    @ResponseBody
    String registerUser() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/getAppInfo")
    @ResponseBody
    String getAppInfo() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/getUserInfo")
    @ResponseBody
    String getUserInfo() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/checkToken")
    @ResponseBody
    String checkToken() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	public static void main(String[] args) throws Exception {
        SpringApplication.run(ACMain.class, args);
    }
}
