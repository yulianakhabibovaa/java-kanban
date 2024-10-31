package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.HttpTaskServer;

import java.io.IOException;
import java.util.Objects;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        if (Objects.requireNonNull(endpoint) == Endpoint.GET) {
            handleGetHistory(exchange);
        } else {
            sendText(exchange, 404, "Такого эндпоинта не существует, проверьте запрос!");
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        sendText(exchange, 200, gson.toJson(HttpTaskServer.getManager().getHistory()));
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("history") && requestMethod.equals("GET")) {
            return Endpoint.GET;
        }

        return Endpoint.UNKNOWN;
    }

    private enum Endpoint {
        GET, UNKNOWN
    }
}
