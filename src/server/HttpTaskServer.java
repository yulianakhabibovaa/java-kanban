package server;

import com.sun.net.httpserver.HttpServer;
import handlers.EpicsHandler;
import handlers.HistoryHandler;
import handlers.PrioritizedHandler;
import handlers.SubTasksHandler;
import handlers.TasksHandler;

import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;

    private static HttpServer httpServer;

    private final TaskManager manager;

    public HttpTaskServer(TaskManager manager) throws IOException {

        this.manager = manager;
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler(manager));
        httpServer.createContext("/subtasks", new SubTasksHandler(manager));
        httpServer.createContext("/epics", new EpicsHandler(manager));
        httpServer.createContext("/history", new HistoryHandler(manager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(manager));

    }

    public static void main(String[] args) {
        start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public static void start() {
        httpServer.start();
    }

    public static void stop() {
        httpServer.stop(0);
    }
}
