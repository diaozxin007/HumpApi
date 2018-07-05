package com.xilidou.handler;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.xilidou.utils.Md5Utils;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhengxin
 */
@Component
public class VariableHandler {

	private static final String url = "http://openapi.youdao.com/api";

	@Autowired
	private JiebaSegmenter segmenter;

	@Autowired
	WebClient client;

	public void get(RoutingContext routingContext){

		String param = routingContext.request().getParam("q");

		HttpServerResponse response =routingContext.response();
		response.putHeader("content-type","application/json");

		List<SegToken> segTokens = segmenter.process(param, JiebaSegmenter.SegMode.INDEX);


		response.end(segmenter.process(param, JiebaSegmenter.SegMode.INDEX).toString());
	}

	private void request(String param){
		HttpRequest request = client.get(url);

		String appKey = "162b04f66a59021c";

		String secretKey = "oowJAjFqEbLVWliF63S5hUo847bAfRzV";

		long timeMillis = System.currentTimeMillis();
		String salt = String.valueOf(timeMillis);

		String sign = Md5Utils.md5(appKey + param + salt + secretKey);

		request.addQueryParam("q",param);
		request.addQueryParam("from","auto");
		request.addQueryParam("to","auto");
		request.addQueryParam("appKey",appKey);
		request.addQueryParam("salt",salt);
		request.addQueryParam("sign",salt);

		return;
	}

}
