package com.xilidou.handler;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.xilidou.entity.ApiResponse;
import com.xilidou.utils.JsonUtils;
import com.xilidou.utils.Md5Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhengxin
 */
@Component
public class VariableHandler {

	private static final String url = "http://openapi.youdao.com/api";

	@Autowired
	private JiebaSegmenter segmenter;

	@Autowired
	private RestTemplate restTemplate;

	public void get(RoutingContext routingContext){

		String param = routingContext.request().getParam("q");


		ApiResponse result = request(param);

		HttpServerResponse response =routingContext.response();
		response.putHeader("content-type","application/json");

		List<SegToken> segTokens = segmenter.process(param, JiebaSegmenter.SegMode.INDEX);



		response.end(JsonUtils.writeValue(result));


//		response.end(segmenter.process(param, JiebaSegmenter.SegMode.INDEX).toString());
	}

	private ApiResponse request(String param){
		String appKey = "162b04f66a59021c";
		String secretKey = "oowJAjFqEbLVWliF63S5hUo847bAfRzV";
		long timeMillis = System.currentTimeMillis();
		String salt = String.valueOf(timeMillis);
		String sign = Md5Utils.md5(appKey + param + salt + secretKey);
		MultiValueMap<String,String> bodyMap = new LinkedMultiValueMap<>();
		bodyMap.add("q",param);
		bodyMap.add("from","auto");
		bodyMap.add("to","auto");
		bodyMap.add("appKey",appKey);
		bodyMap.add("salt",salt);
		bodyMap.add("sign",sign);
		MultiValueMap<String,String> headersMap = new LinkedMultiValueMap<>();
		HttpEntity<MultiValueMap<String, String>> requestEntity  = new HttpEntity<>(bodyMap, headersMap);
		return restTemplate.postForObject(url, requestEntity,ApiResponse.class);





	}

}
