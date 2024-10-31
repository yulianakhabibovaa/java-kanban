package server;

import com.sun.net.httpserver.HttpServer;
import handlers.EpicsHandler;
import handlers.HistoryHandler;
import handlers.PrioritizedHandler;
import handlers.SubTasksHandler;
import handlers.TasksHandler;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;

    private static HttpServer httpServer;

    private static TaskManager manager;

    public static void main(String[] args) throws IOException {
        start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public static void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TasksHandler());
        httpServer.createContext("/subtasks", new SubTasksHandler());
        httpServer.createContext("/epics", new EpicsHandler());
        httpServer.createContext("/history", new HistoryHandler());
        httpServer.createContext("/prioritized", new PrioritizedHandler());

        httpServer.start();
    }

    public static void stop() {
        httpServer.stop(0);
    }

    public static TaskManager getManager() {
        if (manager == null) {
            manager = Managers.getDefault();
        }
        return manager;
    }

    public static void setManager(TaskManager newManager) {
        manager = newManager;
    }
}
