package handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.ManagerTimeCrossingException;
import exceptions.NotFoundException;
import server.HttpTaskServer;
import tasks.Epic;
import tasks.SubTask;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_EPICS: {
                handleGetEpics(exchange);
                break;
            }
            case GET_EPIC_BY_ID: {
                handleGetEpicById(exchange);
                break;
            }
            case POST_EPIC: {
                handlePostEpic(exchange);
                break;
            }
            case DELETE_EPIC_BY_ID: {
                handleDeleteEpicById(exchange);
                break;
            }
            case GET_EPIC_SUBTASKS: {
                handleGetEpicSubtasks(exchange);
                break;
            }
            default:
                sendText(exchange, 404, "Такого эндпоинта не существует, проверьте запрос!");
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        Optional<Integer> postIdOpt = getTaskId(exchange);
        if (postIdOpt.isEmpty()) {
            sendIncorrectId(exchange);
        }

        try {
            List<SubTask> subtasks = HttpTaskServer.getManager().getSubTasksByEpic(postIdOpt.get());
            sendText(exchange, 200, gson.toJson(subtasks));
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }

    private void handleDeleteEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> taskIdOpt = getTaskId(exchange);
        if (taskIdOpt.isEmpty()) {
            sendIncorrectId(exchange);
        }

        HttpTaskServer.getManager().clearEpicById(taskIdOpt.get());
        sendText(exchange, 200, "Эпик с id " + taskIdOpt.get() + " удален");
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);

        try {
            Epic epic = gson.fromJson(body, Epic.class);
            Epic saved;
            if (epic.getId() == null) {
                saved = HttpTaskServer.getManager().create(epic);
            } else {
                saved = HttpTaskServer.getManager().update(epic);
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

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> postIdOpt = getTaskId(exchange);
        if (postIdOpt.isEmpty()) {
            sendIncorrectId(exchange);
        }

        try {
            Epic epic = HttpTaskServer.getManager().getEpicById(postIdOpt.get());
            sendText(exchange, 200, gson.toJson(epic));
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        sendText(exchange, 200, gson.toJson(HttpTaskServer.getManager().getEpics()));
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPICS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_EPIC;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPIC_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return Endpoint.DELETE_EPIC_BY_ID;
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals("epics") && pathParts[3].equals("subtasks")) {
            return Endpoint.GET_EPIC_SUBTASKS;
        }
        return Endpoint.UNKNOWN;
    }

    private enum Endpoint {
        GET_EPICS, GET_EPIC_BY_ID, GET_EPIC_SUBTASKS, POST_EPIC, DELETE_EPIC_BY_ID, UNKNOWN
    }
}
