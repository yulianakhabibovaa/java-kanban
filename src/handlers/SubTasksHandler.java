package handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.ManagerTimeCrossingException;
import exceptions.NotFoundException;
import server.HttpTaskServer;
import tasks.SubTask;

import java.io.IOException;
import java.util.Optional;

public class SubTasksHandler extends BaseHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_SUBTASKS: {
                handleGetSubTasks(exchange);
                break;
            }
            case GET_SUBTASK_BY_ID: {
                handleGetSubTaskById(exchange);
                break;
            }
            case POST_SUBTASK: {
                handlePostSubTask(exchange);
                break;
            }
            case DELETE_SUBTASK_BY_ID: {
                handleDeleteSubTaskById(exchange);
                break;
            }
            default:
                sendText(exchange, 404, "Такого эндпоинта не существует, проверьте запрос!");
        }
    }

    private void handleDeleteSubTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            sendIncorrectId(exchange);
        }

        HttpTaskServer.getManager().clearSubTaskById(taskIdOpt.get());
        sendText(exchange, 200, "Подзадача с id " + taskIdOpt.get() + " удалена");
    }

    private void handlePostSubTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

        try {
            SubTask subtask = gson.fromJson(body, SubTask.class);
            SubTask saved;
            if (subtask.getId() == null) {
                saved = HttpTaskServer.getManager().create(subtask);
            } else {
                saved = HttpTaskServer.getManager().update(subtask);
            }
            sendText(exchange, 201, gson.toJson(saved));
        } catch (JsonSyntaxException e) {
            sendText(exchange, 400, "Тело запроса не соответствует формату json! " + e.getMessage());
        } catch (ManagerTimeCrossingException e) {
            sendHasInteractions(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }

    private void handleGetSubTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> postIdOpt = getTaskId(exchange);
        if (postIdOpt.isEmpty()) {
            sendIncorrectId(exchange);
        }

        try {
            SubTask subtask = HttpTaskServer.getManager().getSubTaskById(postIdOpt.get());
            sendText(exchange, 200, gson.toJson(subtask));
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }

    private void handleGetSubTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, 200, gson.toJson(HttpTaskServer.getManager().getSubTasks()));
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_SUBTASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_SUBTASK_BY_ID;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private enum Endpoint {
        GET_SUBTASKS, GET_SUBTASK_BY_ID, POST_SUBTASK, DELETE_SUBTASK_BY_ID, UNKNOWN
    }
}
