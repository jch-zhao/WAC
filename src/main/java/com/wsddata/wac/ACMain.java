package com.wsddata.wac;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


import redis.clients.jedis.*;

@ComponentScan("com.wsddata.wac,com.wsddata.wac.controller,com.wsddata.ipa")
@EnableScheduling
@EnableAutoConfiguration
public class ACMain {
	private Jedis jedis;
	private JedisPool jedisPool;

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

	@Scheduled(fixedRate = 6000000)//每一分钟清除过期的token
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
	
	public static void main(String[] args) throws Exception {
        SpringApplication.run(ACMain.class, args);
    }
}
