package handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.ManagerTimeCrossingException;
import exceptions.NotFoundException;
import manager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    public TasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                handleGetTasks(exchange);
                break;
            }
            case GET_TASK_BY_ID: {
                handleGetTaskById(exchange);
                break;
            }
            case POST_TASK: {
                handlePostTask(exchange);
                break;
            }
            case DELETE_TASK_BY_ID: {
                handleDeleteTaskById(exchange);
                break;
            }
            default:
                sendText(exchange, 404, "Такого эндпоинта не существует, проверьте запрос!");
        }
    }

    private void handleDeleteTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            sendIncorrectId(exchange);
        }

        manager.clearTaskById(taskIdOpt.get());
        sendText(exchange, 200, "Задача с id " + taskIdOpt.get() + " удалена");
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

        try {
            Task task = gson.fromJson(body, Task.class);
            Task saved;
            if (task.getId() == null) {
                saved = manager.create(task);
            } else {
                saved = manager.update(task);
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

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> postIdOpt = getTaskId(exchange);
        if (postIdOpt.isEmpty()) {
            sendIncorrectId(exchange);
        }

        try {
            Task task = manager.getTaskById(postIdOpt.get());
            sendText(exchange, 200, gson.toJson(task));
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, 200, gson.toJson(manager.getTasks()));
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_TASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_TASK_BY_ID;
            }
        }
        return Endpoint.UNKNOWN;
    }

    private enum Endpoint {
        GET_TASKS, GET_TASK_BY_ID, POST_TASK, DELETE_TASK_BY_ID, UNKNOWN
    }
}
