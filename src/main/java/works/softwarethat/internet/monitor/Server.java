package works.softwarethat.internet.monitor;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Morten Andersen (mortena@gmail.com)
 */
public class Server {
    private static Logger LOGGER = Logger.getLogger(Server.class.getName());

    public Server(String[] args) {
        LOGGER.log(Level.INFO, "Starting SERVER");

        Vertx vertx = Vertx.vertx();

        HttpServerOptions options = new HttpServerOptions()
                .setPort(Integer.valueOf(args[1]));
        HttpServer httpServer = vertx.createHttpServer(options)
                .requestHandler(httpServerRequest -> {
                    if (httpServerRequest.method() == HttpMethod.OPTIONS) {
                        httpServerRequest.response().setStatusCode(200).end();
                        return;
                    }
                    if (httpServerRequest.path().indexOf("favicon") >= 0) {
                        httpServerRequest.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
                        return;
                    }
                    String expectedResponseTime = httpServerRequest.getParam("expectedResponseTime");
                    if (expectedResponseTime != null) {
                        Long aLong = null;
                        try {
                            aLong = Long.valueOf(expectedResponseTime);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        vertx.setTimer(aLong, aLong1 -> {
                            httpServerRequest.response().setStatusCode(200).end("It worked");
                        });
                    } else {
                        httpServerRequest.response().setStatusCode(200).end();
                    }
                });

        httpServer.listen();
    }
}
