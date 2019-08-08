package sf.sf;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class TestServer {
    public static void main( String[] args )
    {
        Vertx vertx = Vertx.vertx();

		HttpServerOptions options = new HttpServerOptions().setMaxWebsocketFrameSize(1000000);

		HttpServer server = vertx.createHttpServer(options);
		server.requestHandler(
				(HttpServerRequest request) -> {
				  HttpServerResponse response = request.response();
			      response.putHeader("content-type", "text/html").
			      	end("<html><body><h1>Hello AAAA from vert.x!</h1></body></html>");

				  /*vertx.setTimer(3000, id -> {
					  System.out.println("ending the response after 3 secs");
					  response.end("Hello world");				  
					});
				  */
			});
		
		server.listen(8080, "0.0.0.0");

	}
}
