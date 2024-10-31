package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import handlers.adapters.DurationAdapter;
import handlers.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class BaseHttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .serializeNulls()
            .create();

    protected void sendText(HttpExchange exchange, int responseCode, String text) throws IOException {
        byte[] response = text.getBytes(DEFAULT_CHARSET);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(responseCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, 404, "По переданному id ничего не найдено!");
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, 406, "Задача пересекается по времени с существующими!");
    }

    protected void sendIncorrectId(HttpExchange exchange) throws IOException {
        sendText(exchange, 400, "Некорректный идентификатор в запросе!");
    }

    protected void sendServerError(HttpExchange exchange) throws IOException {
        sendText(exchange, 500, "Ошибка сервера, попробуйте позже!");
    }

    protected Optional<Integer> getTaskId(HttpExchange exchange) {
        try {
            return Optional.of(Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
