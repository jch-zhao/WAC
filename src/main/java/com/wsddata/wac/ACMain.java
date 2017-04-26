package com.wsddata.wac;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.*;

@Controller
@EnableAutoConfiguration
@ComponentScan("com.wsddata.wac,com.wsddata.ipa")
@EnableScheduling
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
			System.out.println("无法连接redis数据库");
		}
	}
	
	@RequestMapping("/1.0/login")
    @ResponseBody
    String login(HttpServletRequest request) {
		String result=null;
		String systemId = request.getHeader("SystemId");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		jedis=jedisPool.getResource();
		
		List<String> user=jedis.hmget("User",systemId+":"+username,password);
		if(password.equals(user.get(0))){
			String token=generateToken();
			jedis.zadd("TokenPool",new Date().getTime(),token);
			request.getSession().setAttribute("token",token);
			result="{'successful':true,'message':'ok'}";
		}else{
			result="{'successful':true,'message':'ok'}";
		}
        return result;
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
    String registerApp(HttpServletRequest request) {
		//jedis=jedisPool.getResource();
		//jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = "registerApp";
        return value;
    }
	
	@RequestMapping("/approval/registerUser")
    @ResponseBody
    String registerUser(HttpServletRequest request) {
		//注册用户，必须的参数是系统Id、userId
		//jedis=jedisPool.getResource();
		//jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = "approval";
        return value;
    }
	
	@RequestMapping("/1.0/getAppInfo")
    @ResponseBody
    String getAppInfo(HttpServletRequest request) {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/1.0/getUserInfo")
    @ResponseBody
    String getUserInfo(HttpServletRequest request) {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/1.0/changePassword")
    @ResponseBody
    String changePassword(HttpServletRequest request) {
		jedis=jedisPool.getResource();
		//
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/0.1/checkToken")
    @ResponseBody
    String checkTokenTest(HttpServletRequest request) {
		jedis=jedisPool.getResource();
		jedis.set("user", "{'id':'1','username:wangfang','organization:calis'}");
		String value = jedis.get("user");
        return value;
    }
	
	@RequestMapping("/1.0/checkToken")
    @ResponseBody
    String checkToken(HttpServletRequest request) {
		String result = null;
		String token = request.getParameter("token");
		
		jedis=jedisPool.getResource();
		if(jedis.zscore("TokenPool",token)!=null){
			jedis.zadd("TokenPool",new Date().getTime(),token);
			result="{'successful':true,'message':'ok'}";
		}else{
			result="{'successful':false,'message':'No login'}";
		}
        return result;
    }

	@Scheduled(fixedRate = 60000)//每一分钟清除过期的token
    public void cleanToken() throws InterruptedException {
        //当前时间30分钟以前的token被删除
		jedis=jedisPool.getResource();
		Long now=new Date().getTime();
		Set<String> expired=jedis.zrangeByScore("TokenPool",0,now-30*60*1000);
		Iterator<String> i = expired.iterator();
		System.out.println("准备清除"+expired.size()+"个token");
		while(i.hasNext()){
			jedis.zrem("TokenPool",i.next());
		}
    }

	private String generateToken(){
		return "";
	}
	
	public static void main(String[] args) throws Exception {
        SpringApplication.run(ACMain.class, args);
    }
}
