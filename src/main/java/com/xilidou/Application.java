package com.xilidou;

import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * @author zhengxin
 */
@SpringBootApplication
public class Application {

	@Autowired
	private StaticServer staticServer;

	public static void main(String[] args) {
		SpringApplication.run(Application.class,args);
	}

	@PostConstruct
	public void deployVerticle(){
		Vertx.vertx().deployVerticle(staticServer);
	}


}
