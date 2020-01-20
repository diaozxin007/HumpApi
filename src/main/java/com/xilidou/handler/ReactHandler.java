package com.xilidou.handler;

import com.fasterxml.jackson.core.JsonParser;
import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.xilidou.Constants;
import com.xilidou.entity.ApiResponse;
import com.xilidou.serivce.FormatService;
import com.xilidou.utils.JsonUtils;
import com.xilidou.utils.Md5Utils;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhengxin
 */
@Component
@Slf4j
public class ReactHandler {

	@Value("${url}")
	private String url;

	@Value("${appKey}")
	private String appKey;

	@Value("${secretKey}")
	private String secretKey;

	@Autowired
	private WebClient webClient;

	@Autowired
	private JiebaSegmenter segmenter;

	@Autowired
	private FormatService formatService;

	public void get(RoutingContext routingContext) {

		String param = routingContext.request().getParam("q");
		String statusStr = routingContext.request().getParam("status");

		int status = NumberUtils.toInt(statusStr, 0);

		log.info("q is {}, and status is {}", param, statusStr);

		HttpServerResponse response = routingContext.response();
		List<String> result = new ArrayList<>();

		if (StringUtils.isBlank(param)) {
			result.add("request is null");
			response.end(JsonUtils.writeValue(result));
			return;
		}

		splitKeywords(param,t->{

			List<String> splitKey = t.result();

		});

		response.putHeader("content-type", "application/json");

		postHttp(param,handler->{
			if(handler.succeeded()){
				String json = handler.result();
				ApiResponse apiResponse = JsonUtils.readValue(json,ApiResponse.class);
				List<String> translations = formatService.getTranslations(apiResponse,status);
				JsonArray objects = new JsonArray(translations);
				response.end(objects.encodePrettily());

			}else{
				response.end(handler.cause().getMessage());
			}
		});
	}

	private void splitKeywords(String param,Handler<AsyncResult<List<String>>> handler){
		List<SegToken> process = segmenter.process(param, JiebaSegmenter.SegMode.SEARCH);
		if(CollectionUtils.isNotEmpty(process)){
			List<String> collect = process.stream().map(t -> t.word).collect(Collectors.toList());
			log.info("key is {}",collect);
			Future.succeededFuture(collect).setHandler(handler);

		}

	}


	private void postHttp(String param,Handler<AsyncResult<String>> handler){

		webClient.post(url,"/api")
				.sendForm(requestFrom(param),ar->{
					Future.succeededFuture(ar.result().bodyAsString()).setHandler(handler);
				});
	}

	private MultiMap requestFrom(String param){
		long timeMillis = System.currentTimeMillis();
		String salt = String.valueOf(timeMillis);
		String sign = Md5Utils.md5(appKey + param + salt + secretKey);
		MultiMap form = MultiMap.caseInsensitiveMultiMap();
		form.set("q",param);
		form.set("from","auto");
		form.set("to","auto");
		form.set("appKey",appKey);
		form.set("salt",salt);
		form.set("sign",sign);
		return form;
	}



}
