package com.xilidou.handler;

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.xilidou.Constants;
import com.xilidou.entity.ApiResponse;
import com.xilidou.serivce.CounterService;
import com.xilidou.serivce.FormatService;
import com.xilidou.utils.JsonUtils;
import com.xilidou.utils.Md5Utils;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhengxin
 */
@Component
@Slf4j
public class VariableHandler {

	private static final String URL = "http://openapi.youdao.com/api";

	@Value("${url}")
	private String url;

	@Value("${appKey}")
    private String appKey;

	@Value("${secretKey}")
    private String secretKey;

	@Autowired
	private JiebaSegmenter segmenter;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
    private Cache<String,ApiResponse> cache;

	@Autowired
	private FormatService formatService;

	@Autowired
	private CounterService counterService;

	//1.直接翻译
	//2.分词然后组合

	public void get(RoutingContext routingContext){

		String param = routingContext.request().getParam("q");
		String statusStr =routingContext.request().getParam("status");

		log.info("q is {}, and status is {}",param,statusStr);

		counterService.add(1);

        HttpServerResponse response =routingContext.response();
        response.putHeader("content-type","application/json");
        List<String> result = new ArrayList<>();

		if(StringUtils.isBlank(param)){
		    result.add("request is null");
            response.end(JsonUtils.writeValue(result));
            return;
        }

		int status = NumberUtils.toInt(statusStr,0);
		ApiResponse apiResponse = cachedResponse(param);

        List<String> translations = formatService.getTranslations(apiResponse,status);
        result.addAll(translations);

        List<SegToken> process = segmenter.process(param, JiebaSegmenter.SegMode.SEARCH);
        if(process.size() >= 2){
            List<ApiResponse> apiResponses = process.parallelStream()
                    .map(t -> cachedResponse(t.word))
                    .collect(Collectors.toList());

            List<String> words = apiResponses.stream().map(formatService::getWord).collect(Collectors.toList());

            String join = Joiner.on("_").join(words);
            String s = formatService.formatString(join,status);
            result.add(s);
        }
        response.end(JsonUtils.writeValue(result));
	}



    private ApiResponse cachedResponse(String param){
	    try {

            return cache.get(param, () -> requestYoudao(param));
        }catch (Exception e){
	        log.error("error",e);
        }
        return null;
    }

	private ApiResponse requestYoudao(String param){
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
