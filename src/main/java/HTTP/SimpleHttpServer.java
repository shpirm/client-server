package HTTP;

import Shop.Product;
import Shop.ShopDatabase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class SimpleHttpServer {
    public final static ShopDatabase db = new ShopDatabase();
    public final static LoginDatabase udb = new LoginDatabase();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String AUTH_HEADER = "Authentication";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8765), 0);

        HttpContext context = server.createContext("/", new EchoHandler());
        context.setAuthenticator(new Auth());
        db.initialization();
        udb.initialization();

        server.setExecutor(null);
        server.start();
    }

    static class EchoHandler implements HttpHandler {

        private final List<EndPointHandler> handlers = List.of(
                new EndPointHandler("/api/good/?", "GET", this::processGetAll),
                new EndPointHandler("/api/good/?", "PUT", exchange -> {
                }),
                new EndPointHandler("/api/good/(\\d+)", "GET", exchange -> {
                }),
                new EndPointHandler("/api/good/(\\d+)", "DELETE", exchange -> {
                }),
                new EndPointHandler("/api/good/(\\d+)", "POST", exchange -> {
                }),
                new EndPointHandler("/login", "POST", exchange -> {
                })
        );

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            handlers.stream()
                    .filter(endpointHandler -> endpointHandler.isMatch(exchange))
                    .findFirst()
                    .ifPresentOrElse(endpointHandler -> endpointHandler.handle(exchange),
                            processUnknownEndpoint(exchange));
        }

        private static class EndPointHandler {
            private final String pathPattern, httpMethod;
            private final Consumer<HttpExchange> handler;


            public EndPointHandler(String pathPattern, String httpMethod, Consumer<HttpExchange> handler) {
                this.pathPattern = pathPattern;
                this.httpMethod = httpMethod;
                this.handler = handler;
            }

            public void handle(HttpExchange exchange) {
                exchange.getResponseHeaders().put("content-type", List.of("application-json"));
                handler.accept(exchange);
            }

            public boolean isMatch(HttpExchange exchange) {
                if (!exchange.getRequestMethod().equals(httpMethod)) return false;
                String path = exchange.getRequestURI().getPath();
                return path.matches(pathPattern);
            }
        }

        private Runnable processUnknownEndpoint(HttpExchange exchange) {
            return new Runnable() {
                String result = "Error! Unknown endpoint.";

                @Override
                public void run() {
                    try {
                        sendMessage(exchange, 404, result);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        private void processGetAll(HttpExchange exchange) throws IOException {
            List<Product> products = null;
            try {
                products = db.getProductList("ProductID");
                sendMessage(exchange, 200, products);
            } catch (SQLException | IOException e) {
                String result = "Error! Unknown endpoint.";
                sendMessage(exchange, 404, result);
            }
        }
        private void processGetByID(HttpExchange exchange) throws IOException {
            String id = exchange.getRequestURI().getPath().replace("/api/good", "");
            try {
                Product product = db.readProductById(Integer.parseInt(id));
                sendMessage(exchange, 200, product);
            } catch (SQLException | IOException e) {
                String result = "Error! Unknown endpoint.";
                sendMessage(exchange, 404, result);
            }
        }

        private void sendMessage(HttpExchange exchange, int code, Object answer) throws IOException {
            byte[] data = OBJECT_MAPPER.writeValueAsBytes(answer);
            exchange.sendResponseHeaders(code, data.length);
            OutputStream os = exchange.getResponseBody();
            os.write(data);
            os.close();
        }
    }


    static class Auth extends Authenticator {
        @Override
        public Result authenticate(HttpExchange httpExchange) {
            final String token = httpExchange.getResponseHeaders().getFirst(AUTH_HEADER);
            if (token != null) {
                try {
                    String user = JwtService.getUsernameFromToken(token);
                    Connection connection = udb.getUserByLogin(user);
                    if (connection != null) return new Success(new HttpPrincipal(user, "get"));
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            return new Failure(403);
        }
    }
}