package server;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
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

public class HttpTaskServerPrioritizedTest {

    InMemoryTaskManager manager;
    HttpTaskServer server;
    HttpClient client = HttpClient.newHttpClient();
    LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void initServer() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void shouldGetPrioritized() throws IOException, InterruptedException {
        Task task = new Task("Test", "Testing task", Status.NEW, null, Duration.ofMinutes(5), now);
        Epic epic = new Epic("epic", "test epic");
        manager.create(task);
        manager.create(epic);
        SubTask subTask = new SubTask("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10), epic.getId());
        manager.create(subTask);

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritizedFromManager = manager.getPrioritizedTasks();
        List<Task> history = gson.fromJson(response.body(), new HttpTaskServerTasksTest.TaskListTypeToken().getType());
        assertEquals(prioritizedFromManager.getFirst(), history.getFirst());
        assertEquals(prioritizedFromManager.getLast().getTitle(), history.getLast().getTitle());
    }
}
