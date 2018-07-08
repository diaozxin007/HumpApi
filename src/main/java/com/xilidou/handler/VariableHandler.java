package com.xilidou.handler;

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.xilidou.Constants;
import com.xilidou.entity.ApiResponse;
import com.xilidou.utils.JsonUtils;
import com.xilidou.utils.Md5Utils;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
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

	//1.直接翻译
	//2.分词然后组合

	public void get(RoutingContext routingContext){

		String param = routingContext.request().getParam("q");
		String statusStr = routingContext.request().getParam("status");

		log.info("q is {}, and status is {}",param,statusStr);

		int status = NumberUtils.toInt(statusStr,0);

		List<String> result = new ArrayList<>();

		HttpServerResponse response =routingContext.response();
		response.putHeader("content-type","application/json");

		ApiResponse apiResponse = cachedResponse(param);
        List<String> translations = getTranslations(apiResponse,status);
        result.addAll(translations);

        List<SegToken> process = segmenter.process(param, JiebaSegmenter.SegMode.SEARCH);
        if(process.size() >= 2){
            List<ApiResponse> apiResponses = process.parallelStream()
                    .map(t -> cachedResponse(t.word))
                    .collect(Collectors.toList());

            List<String> words = apiResponses.stream().map(this::getWord).collect(Collectors.toList());

            String join = Joiner.on("_").join(words);
            String s = formatString(join,status);
            result.add(s);
        }


        response.end(JsonUtils.writeValue(result));
	}

	private List<String> getTranslations(ApiResponse apiResponse,int status){
	    List<String> translations = apiResponse.getTranslation();
        List<String> subList = translations.subList(0, 1);
        return subList.parallelStream().map(t->formatString(t,status)).collect(Collectors.toList());
    }

	private String getWord(ApiResponse apiResponse){
        if(apiResponse.getErrorCode() == 0 && apiResponse.getBasic() != null){
            return apiResponse.getBasic().getExplains().get(0);
        }

        else {
            return apiResponse.getTranslation().get(0);
        }
    }

	private String formatString(String str,int status){
        String underline = CharMatcher.whitespace().trimAndCollapseFrom(str,'_');
        underline = CharMatcher.inRange('a','z')
                .or(CharMatcher.inRange('A','Z'))
                .or(CharMatcher.is('_'))
                .retainFrom(underline);
        if(underline.startsWith("_")){
            underline = underline.substring(1);
        }

        switch (status){
            case Constants.LOWER_CAMEL:
                return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,underline);
            case Constants.LOWER_HYPHEN:
                return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN,underline);
            case Constants.LOWER_UNDERSCORE:
                return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE,underline);
            case Constants.UPPER_CAMEL:
                return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,underline);
            case Constants.UPPER_UNDERSCORE:
                return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE,underline);
            default:
                return  CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,underline);
        }

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
