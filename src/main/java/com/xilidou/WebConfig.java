package com.xilidou;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

	@Bean
	public JiebaSegmenter jiebaSegmenter(){
		return new JiebaSegmenter();
	}

    @Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
    }

}
