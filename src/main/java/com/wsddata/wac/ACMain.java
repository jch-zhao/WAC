package com.wsddata.wac;

import javax.servlet.http.HttpServletRequest;

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
@ComponentScan("com.wsddata.wac,com.wsddata.ipa")
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
	
	@RequestMapping("/1.0/login")
    @ResponseBody
    String login(HttpServletRequest request) {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		//request.getSession().setAttribute("token",value+"is token");
        return value;
    }
	
	@RequestMapping("/admin/logout")
    @ResponseBody
    String logout(HttpServletRequest request) {
		//jedis=jedisPool.getResource();
		//jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = "logout";
        return value;
    }
	
	@RequestMapping("/service/registerApp")
    @ResponseBody
    String registerApp() {
		//jedis=jedisPool.getResource();
		//jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = "registerApp";
        return value;
    }
	
	@RequestMapping("/approval/registerUser")
    @ResponseBody
    String registerUser() {
		//jedis=jedisPool.getResource();
		//jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = "approval";
        return value;
    }
	
	@RequestMapping("/1.0/getAppInfo")
    @ResponseBody
    String getAppInfo() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/1.0/getUserInfo")
    @ResponseBody
    String getUserInfo() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/1.0/checkToken")
    @ResponseBody
    String checkToken() {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/2.0/checkToken")
    @ResponseBody
    String checkToken2() {
		//jedis=jedisPool.getResource();
		//jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = "2.0/checkToken";
        return value;
    }
	
	public static void main(String[] args) throws Exception {
        SpringApplication.run(ACMain.class, args);
    }
}
