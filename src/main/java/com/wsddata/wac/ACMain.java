package com.wsddata.wac;

import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import sun.misc.BASE64Encoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import redis.clients.jedis.*;

@RestController
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
	
	@RequestMapping(value="/1.0/login",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    String login(HttpServletRequest request,String username,String password) {
		String result=null;
		String systemId = request.getHeader("systemId");
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
	
	@RequestMapping(value="/admin/logout",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    String logout(String token) {
		String result=null;
		try{
			jedis=jedisPool.getResource();
			jedis.zrem("TokenPool",token);
			result="{'successful':true,'message':'ok'}";
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping(value="/service/registerSystem",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    String registerApp(HttpServletRequest request,String sysId,String sysInfo) {
		String result=null;
		String systemId = request.getHeader("systemId");
		if(systemId==null||sysId==null||!systemId.equals(sysId)){
			result="{'successful':false,'error':'Missing parameters or parameters not matched'}";
			return result;
		}
		if(sysInfo==null){
			sysInfo="";
		}
		try{
			jedis=jedisPool.getResource();
			jedis.set("System:"+sysId,sysInfo);
			result="{'successful':true,'message':'ok'}";
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping(value="/approval/registerUser",method=RequestMethod.POST , produces="application/json;charset=UTF-8")
    @ResponseBody
    String registerUser(HttpServletRequest request, String username,String password,String userInfo) {
		String result=null;
		
		String systemId = request.getHeader("systemId");
		if(systemId==null||systemId.equals("")||username==null||username.equals("")){
			result="{'successful':false,'error':'Missing parameters'}";
			return result;
		}
		if(password==null){
			password="";
		}
		try{
			jedis=jedisPool.getResource();
			String existUser=null;
			existUser=jedis.hget("",username);
			//判断重名
			if(existUser!=null){
				result="{'successful':false,'error':'User exist'}";
			}else{
				HashMap<String, String> user = new HashMap();
				user.put("username",username);
				user.put("password",password);
				user.put("userInfo",userInfo);
				jedis.hmset(systemId+":"+username,user);
				result="{'successful':true,'message':'ok'}";
			}
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/getAppInfo",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    String getAppInfo(String sysId) {
		String result=null;
		
		if(sysId==null){
			result="{'successful':false,'error':'Missing parameters'}";
			return result;
		}
		try{
			jedis=jedisPool.getResource();
			String sysInfo=null;
			sysInfo=jedis.get("System:"+sysId);
			if(sysInfo!=null){
				result="{'successful':true,'sysInfo':'"+sysInfo+"'}";
			}
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/getUserInfo",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    String getUserInfo(HttpServletRequest request, String username) {
		String result=null;
		
		String systemId = request.getHeader("systemId");
		if(systemId==null||systemId.equals("")||username==null||username.equals("")){
			result="{'successful':false,'error':'Missing parameters'}";
			return result;
		}
		try{
			jedis=jedisPool.getResource();
			List<String> userInfo=jedis.hmget(systemId+":"+username,"userInfo");
			if(userInfo.size()>0){
				result="'successful':true,'userInfo':'"+userInfo.get(0)+"'}";
			}else{
				result="'successful':false,'error':'user not exist'}";
			}
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/changePassword",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    String changePassword(HttpServletRequest request, String username, String password) {
		String result=null;

		String systemId = request.getHeader("systemId");
		if(systemId==null||systemId.equals("")||username==null||username.equals("")||password==null){
			result="{'successful':false,'error':'Missing parameters'}";
			return result;
		}
		try{
			jedis=jedisPool.getResource();
			List<String> user=jedis.hmget(systemId+":"+username,"username","userInfo");
			if(user.size()<=0){
				//该用户不存在
				result="{'successful':false,'error':'user not exist'}";
			}else{
				HashMap<String,String> changeUser=new HashMap<String, String>();
				changeUser.put("username",user.get(0));
				changeUser.put("password",password);
				if(user.size()<=1){
					changeUser.put("userInfo","");
				}else{
					changeUser.put("userInfo",user.get(1));
				}
				jedis.hmset(systemId+":"+username,changeUser);
				result="{'successful':true,'message':'ok'}";
			}
		}catch(Exception e){
			result="{'successful':false,'error':'server error'}";
		}finally{
			jedis.close();
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/checkToken",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    String checkToken(String token) {
		String result = null;
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
