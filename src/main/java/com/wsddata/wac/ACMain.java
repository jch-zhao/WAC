package com.wsddata.wac;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import sun.misc.BASE64Encoder;
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
		String systemId = request.getHeader("systemId");
		System.out.println(systemId);
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if(systemId==null||systemId.equals("")||username==null||username.equals("")||password==null){
			result="{'successful':false,'error':'Missing parameters'}";
			return result;
		}
		try{
			jedis=jedisPool.getResource();
			List<String> user=jedis.hmget(systemId+":"+username,"password");
			if(password.equals(user.get(0))){
				String token=generateToken(systemId);
				if(token!=null){
					jedis.zadd("TokenPool",new Date().getTime(),token);
					result="{'successful':true,'token':'"+token+"'}";
				}else{
					result="{'successful':false,'error':'token fail'}";
				}
			}else{
				result="{'successful':false,'error':'login fail'}";
			}
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping("/admin/logout")
    @ResponseBody
    String logout(HttpServletRequest request) {
		String result=null;
		String username = request.getParameter("username");
		try{
			jedis=jedisPool.getResource();
			//从tokenPool中清除此用户的token
			result="";
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping("/service/registerApp")
    @ResponseBody
    String registerApp(HttpServletRequest request) {
		String result=null;
		
		try{
			jedis=jedisPool.getResource();
			result="";
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping("/approval/registerUser")
    @ResponseBody
    String registerUser(HttpServletRequest request) {
		String result=null;
		
		try{
			jedis=jedisPool.getResource();
			result="";
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping("/1.0/getAppInfo")
    @ResponseBody
    String getAppInfo(HttpServletRequest request) {
		String result=null;
		
		try{
			jedis=jedisPool.getResource();
			result="";
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping("/1.0/getUserInfo")
    @ResponseBody
    String getUserInfo(HttpServletRequest request) {
		String result=null;
		
		try{
			jedis=jedisPool.getResource();
			result="";
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping("/1.0/changePassword")
    @ResponseBody
    String changePassword(HttpServletRequest request) {
		String result=null;
		
		try{
			jedis=jedisPool.getResource();
			result="";
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping("/0.1/checkToken")
    @ResponseBody
    String checkTokenTest(HttpServletRequest request) {
		String result=null;
		try{
			jedis=jedisPool.getResource();
			result="";
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping("/1.0/checkToken")
    @ResponseBody
    String checkToken(HttpServletRequest request) {
		String result = null;
		String token = request.getParameter("token");
		try{
			jedis=jedisPool.getResource();
			if(jedis.zscore("TokenPool",token)!=null){
				jedis.zadd("TokenPool",new Date().getTime(),token);
				result="{'successful':true,'message':'ok'}";
			}else{
				result="{'successful':false,'error':'No login'}";
			}
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }

	@Scheduled(fixedRate = 60000)//每一分钟清除过期的token
    public void cleanToken() throws InterruptedException {
		try{
			//当前时间30分钟以前的token被删除
			jedis=jedisPool.getResource();
			Long now=new Date().getTime();
			Set<String> expired=jedis.zrangeByScore("TokenPool",0,now-30*60*1000);
			Iterator<String> i = expired.iterator();
			System.out.println("准备清除"+expired.size()+"个token");
			while(i.hasNext()){
				jedis.zrem("TokenPool",i.next());
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			jedis.close();
		}
    }

	private String generateToken(String sysId){
		BASE64Encoder encoder = new BASE64Encoder();
		MessageDigest md;
		String token=null;
		try{
			Random r=new Random();
			byte[] b=new byte[16];
			r.nextBytes(b);
			String head=encoder.encode(b);
			md = MessageDigest.getInstance("MD5");
			md.update(sysId.getBytes());
			byte[] id=md.digest();
			String tAppid=encoder.encode(id);
			UUID u=UUID.randomUUID();
			String us=u.toString().replace("-","");
			String u_base64=encoder.encode(us.getBytes());
			token=head+tAppid+u_base64;
			token=token.replace("=","");
			token=token.replace("/","");
			token=token.replace("+","");
		}catch(Exception e){
			e.printStackTrace();
		}
		return token;
	}
	
	public static void main(String[] args) throws Exception {
        SpringApplication.run(ACMain.class, args);
    }
}
