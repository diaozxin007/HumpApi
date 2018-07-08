package com.xilidou;

import com.xilidou.handler.VariableHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhengxin
 */
@Component
public class StaticServer extends AbstractVerticle {

	@Autowired
	private VariableHandler variableHandler;

	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.post("/api/hump").handler(routingContext ->variableHandler.get(routingContext));
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);

	}

}
