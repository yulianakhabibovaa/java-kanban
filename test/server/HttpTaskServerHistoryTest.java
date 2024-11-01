package server;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static handlers.BaseHttpHandler.gson;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpTaskServerHistoryTest {

    InMemoryTaskManager manager = new InMemoryTaskManager();
    HttpTaskServer server;
    HttpClient client = HttpClient.newHttpClient();
    LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void initServer() throws IOException {
        manager.clearTasks();
        manager.clearEpics();
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void shouldGetHistory() throws IOException, InterruptedException {
        Epic epic = new Epic("epic", "testing epic");
        Task task = new Task("Test", "Testing task", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10));
        manager.create(task);
        manager.create(epic);
        manager.getTaskById(0);
        manager.getEpicById(1);

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> historyFromManager = manager.getHistory();
        List<Task> history = gson.fromJson(response.body(), new HttpTaskServerTasksTest.TaskListTypeToken().getType());
        assertEquals(historyFromManager.getFirst(), history.getFirst());
        assertEquals(historyFromManager.getLast().getTitle(), history.getLast().getTitle());
    }

}
