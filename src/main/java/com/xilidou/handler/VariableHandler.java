package com.xilidou.handler;

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.xilidou.entity.ApiResponse;
import com.xilidou.utils.JsonUtils;
import com.xilidou.utils.Md5Utils;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

	//1.直接翻译
	//2.分词然后组合

	public void get(RoutingContext routingContext){

		String param = routingContext.request().getParam("q");
		String status = routingContext.request().getParam("status");

		List<String> result = new ArrayList<>();

		HttpServerResponse response =routingContext.response();
		response.putHeader("content-type","application/json");

		ApiResponse apiResponse = requestYoudao(param);
        List<String> translations = getTranslations(apiResponse);
        result.addAll(translations);

        List<SegToken> process = segmenter.process(param, JiebaSegmenter.SegMode.INDEX);
        if(process.size() >= 2){
            List<ApiResponse> apiResponses = process.parallelStream()
                    .map(t -> requestYoudao(t.word))
                    .collect(Collectors.toList());

            List<String> words = apiResponses.stream().map(this::getWord).collect(Collectors.toList());

            String join = Joiner.on("_").join(words);
            String s = formatString(join);
            result.add(s);
        }


        response.end(JsonUtils.writeValue(result));
	}

	private List<String> getTranslations(ApiResponse apiResponse){
	    List<String> translations = apiResponse.getTranslation();
        List<String> subList = translations.subList(0, 1);
        return subList.parallelStream().map(this::formatString).collect(Collectors.toList());
    }

	private String getWord(ApiResponse apiResponse){
        if(apiResponse.getBasic() != null){
            return apiResponse.getBasic().getExplains().get(0);
        }

        else {
            return apiResponse.getTranslation().get(0);
        }
    }

	private String formatString(String str){
        String underline = CharMatcher.whitespace().trimAndCollapseFrom(str,'_');
        underline = CharMatcher.inRange('0','9').or(CharMatcher.inRange('a','z'))
                .or(CharMatcher.inRange('A','Z'))
                .trimFrom(underline);
        return  CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,underline);

    }

	private ApiResponse requestYoudao(String param){
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
