//package com.xilidou;
//
//import com.xilidou.handler.VariableHandler;
//import io.vertx.core.Vertx;
//import io.vertx.core.http.HttpServer;
//import io.vertx.core.http.HttpServerResponse;
//import io.vertx.ext.web.Router;
//
//public class VertxWeb {
//
//	public static void main(String[] args) {
//
//		Vertx vertx = Vertx.vertx();
//
//		HttpServer server = vertx.createHttpServer();
//		Router router = Router.router(vertx);
//
////		router.route().handler(routingContext->{
////
////			HttpServerResponse response =routingContext.response();
////
////			response.putHeader("content-type","text/plain");
////			response.end("Hello world from doudou");
////		});
//
//		router.route("/api").handler(routingContext ->new VariableHandler().get(routingContext));
//		server.requestHandler(router::accept).listen(8080);
//
//
//
//	}
//
//}
