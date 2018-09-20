package com.xilidou;

import com.avos.avoscloud.AVACL;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${applicationId}")
	private String applicationId;

	@Value("${clientKey}")
	private String clientKey;

	@Value("${masterKey}")
	private String masterKey;


	public static void main(String[] args) {
		SpringApplication.run(Application.class,args);
	}

	@PostConstruct
	public void deployVerticle(){
		AVOSCloud.initialize(applicationId,clientKey,masterKey);
		Vertx.vertx().deployVerticle(staticServer);
	}


}
