package com.xilidou;

import com.huaban.analysis.jieba.JiebaSegmenter;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

	@Bean
	public JiebaSegmenter jiebaSegmenter(){
		return new JiebaSegmenter();
	}

	@Bean
	public WebClient webClient(){
		Vertx vertx = Vertx.vertx();
		WebClientOptions options = new WebClientOptions()
				.setUserAgent("My-App/1.2.3");
		options.setKeepAlive(true);
		WebClient client = WebClient.create(vertx, options);
		return client;
	}

}
