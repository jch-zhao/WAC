package com.wsddata.wac.controller;

import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.wsddata.wac.Conf;
import com.wsddata.wac.Result;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import sun.misc.BASE64Encoder;

@RestController
@EnableAutoConfiguration
public class AuthCenter {
	private Jedis jedis;
	private JedisPool jedisPool;
	
	@Autowired
	public AuthCenter(Conf conf){
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
    Result login(HttpServletRequest request,String username,String password) {
		Result result=new Result();
		String systemId = request.getHeader("systemId");
		
		systemId="test";//测试用，正式版删除此行
		if(systemId==null||systemId.equals("")||username==null||username.equals("")||password==null){
			result.setSuccessful(false);
			result.setMessage("Missing parameters");
			result.setData(null);
			return result;
		}
		try{
			jedis=jedisPool.getResource();
			List<String> user=jedis.hmget(systemId+":"+username,"password");
			if(password.equals(user.get(0))){
				String token=generateToken(systemId);
				if(token!=null){
					jedis.zadd("TokenPool",new Date().getTime(),token);
					
					result.setSuccessful(true);
					result.setMessage("Generated Token");
					result.setData(token);
				}else{
					result.setSuccessful(false);
					result.setMessage("Generate Token fail");
					result.setData(null);
				}
			}else{
				result.setSuccessful(false);
				result.setMessage("Login fail");
				result.setData(null);
			}
		}catch(Exception e){
			result.setSuccessful(false);
			result.setMessage("Server error");
			result.setData(null);
		}finally{
			try{
				jedis.close();
			}catch(Exception e){		
			}
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/logout",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    Result logout(HttpServletRequest request, String token) {
		Result result=new Result();
		String systemId = request.getHeader("systemId");
		
		systemId="test";//测试用，正式版删除此行
		if(systemId==null||systemId.equals("")||token==null){
			result.setSuccessful(false);
			result.setMessage("Missing parameters");
			return result;
		}
		try{
			jedis=jedisPool.getResource();
			jedis.zrem("TokenPool",token);
			result.setSuccessful(true);
			result.setMessage("ok");
			
		}catch(Exception e){
			result.setSuccessful(false);
			result.setMessage("server error");
		}finally{
			try{
				jedis.close();
			}catch(Exception e){		
			}
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/registerSystem",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    Result registerApp(HttpServletRequest request,String sysId,String sysInfo) {
		Result result=new Result();
		String systemId = request.getHeader("systemId");
		
		systemId="test1";//测试用，正式版删除此行
		if(systemId==null||sysId==null||!systemId.equals(sysId)){
			result.setSuccessful(false);
			result.setMessage("Missing parameters or parameters not matched");
			return result;
		}
		if(sysInfo==null){
			sysInfo="";
		}
		try{
			//要做判重
			//System.out.println(sysInfo);
			
			String existApp=null;
			jedis=jedisPool.getResource();
			existApp=jedis.get("System:"+sysId);
			if(existApp!=null){
				result.setSuccessful(false);
				result.setMessage("System exist");
				return result;
			}else{
				jedis.set("System:"+sysId,sysInfo);
				result.setSuccessful(true);
				result.setMessage("ok");
			}
		}catch(Exception e){
			result.setSuccessful(false);
			result.setMessage("server error");
		}finally{
			try{
				jedis.close();
			}catch(Exception e){		
			}
			
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/registerUser",method=RequestMethod.GET , produces="application/json;charset=UTF-8")
    @ResponseBody
    Result registerUser(HttpServletRequest request,String username,String password,String userInfo) {
		Result result=new Result();
		String systemId = request.getHeader("systemId");
		
		systemId="test";//测试用，正式版删除此行
		if(systemId==null||systemId.equals("")||username==null||username.equals("")){
			result.setSuccessful(false);
			result.setMessage("Missing parameters");
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
				result.setSuccessful(false);
				result.setMessage("User exist");
			}else{
				HashMap<String, String> user = new HashMap();
				user.put("username",username);
				user.put("password",password);
				user.put("userInfo",userInfo);
				jedis.hmset(systemId+":"+username,user);
				result.setSuccessful(true);
				result.setMessage("ok");
			}
		}catch(Exception e){
			result.setSuccessful(false);
			result.setMessage("server error");
		}finally{
			try{
				jedis.close();
			}catch(Exception e){		
			}
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/getAppInfo",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    Result getAppInfo(HttpServletRequest request, String sysId) {
		Result result=new Result();
		String systemId = request.getHeader("systemId");
		
		systemId="test";//测试用，正式版删除此行
		if(systemId==null||systemId.equals("")||sysId==null){
			result.setSuccessful(false);
			result.setMessage("Missing parameters");
			return result;
		}
		try{
			jedis=jedisPool.getResource();
			String sysInfo=null;
			sysInfo=jedis.get("System:"+sysId);
			if(sysInfo!=null){
				result.setSuccessful(true);
				result.setMessage("ok");
				result.setData("{'sysInfo':'"+sysInfo+"'}");
			}
		}catch(Exception e){
			result.setSuccessful(false);
			result.setMessage("server error");
		}finally{
			try{
				jedis.close();
			}catch(Exception e){		
			}
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/getUserInfo",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    Result getUserInfo(HttpServletRequest request, String username) {
		Result result=new Result();
		String systemId = request.getHeader("systemId");
		
		systemId="test";//测试用，正式版删除此行
		if(systemId==null||systemId.equals("")||username==null||username.equals("")){
			result.setSuccessful(false);
			result.setMessage("Missing parameters");
			return result;
		}
		try{
			jedis=jedisPool.getResource();
			List<String> userInfo=jedis.hmget(systemId+":"+username,"userInfo");
			if(userInfo.size()>0){
				result.setSuccessful(true);
				result.setMessage("ok");
				result.setData("{'userInfo':'"+userInfo.get(0)+"'}");
			}else{
				result.setSuccessful(false);
				result.setMessage("user not exist");
			}
		}catch(Exception e){
			result.setSuccessful(false);
			result.setMessage("server error");
		}finally{
			try{
				jedis.close();
			}catch(Exception e){		
			}
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/changePassword",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    Result changePassword(HttpServletRequest request, String username, String password) {
		Result result=new Result();
		String systemId = request.getHeader("systemId");
		
		systemId="test";//测试用，正式版删除此行
		if(systemId==null||systemId.equals("")||username==null||username.equals("")||password==null){
			result.setSuccessful(false);
			result.setMessage("Missing parameters");
			return result;
		}
		try{
			jedis=jedisPool.getResource();
			List<String> user=jedis.hmget(systemId+":"+username,"username","userInfo");
			if(user.size()<=0){
				//该用户不存在
				result.setSuccessful(false);
				result.setMessage("user not exist");
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
				result.setSuccessful(true);
				result.setMessage("ok");
			}
		}catch(Exception e){
			result.setSuccessful(false);
			result.setMessage("server error");
		}finally{
			try{
				jedis.close();
			}catch(Exception e){		
			}
		}
        return result;
    }
	
	@RequestMapping(value="/1.0/checkToken",method=RequestMethod.GET, produces="application/json;charset=UTF-8")
    @ResponseBody
    Result checkToken(HttpServletRequest request,String token) {
		Result result = new Result();
		String systemId = request.getHeader("systemId");
		
		systemId="test";//测试用，正式版删除此行
		try{
			jedis=jedisPool.getResource();
			if(jedis.zscore("TokenPool",token)!=null){
				jedis.zadd("TokenPool",new Date().getTime(),token);
				result.setSuccessful(true);
				result.setMessage("ok");
			}else{
				result.setSuccessful(false);
				result.setMessage("No login");
			}
		}catch(Exception e){
			result.setSuccessful(false);
			result.setMessage("server error");
		}finally{
			try{
				jedis.close();
			}catch(Exception e){		
			}
		}
        return result;
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

}
