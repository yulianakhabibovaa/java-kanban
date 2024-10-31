package server;

import com.google.gson.reflect.TypeToken;
import manager.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpTaskServerTasksTest {

    InMemoryTaskManager manager;
    HttpTaskServer server;
    HttpClient client = HttpClient.newHttpClient();
    LocalDateTime now = LocalDateTime.now();
    Task task;


    @BeforeEach
    void initServer() throws IOException {
        task = new Task("Test 1", "Testing task 1", Status.NEW, null, Duration.ofMinutes(5), now);
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void shouldCreateTask() throws IOException, InterruptedException {
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 1", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
    }

    @Test
    void shouldFailToCreateTaskIfTimeCrossing() throws IOException, InterruptedException {
        manager.create(task);
        Task task2 = new Task("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now);
        String taskJson = gson.toJson(task2);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается по времени с существующими!", response.body());
    }

    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        manager.create(task);
        Task update = task.copy();
        update.setStatus(Status.IN_PROGRESS);
        update.setTitle("new title");
        String taskJson = gson.toJson(update);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("new title", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
        assertEquals(Status.IN_PROGRESS, tasksFromManager.getFirst().getStatus(), "Некорректный статус");
    }

    @Test
    void shouldFailToUpdateTaskIfTimeCrossing() throws IOException, InterruptedException {
        manager.create(task);
        Task task2 = manager.create(new Task("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10)));
        Task update = task2.copy();
        update.setStartTime(now);
        String taskJson = gson.toJson(update);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается по времени с существующими!", response.body());
    }

    @Test
    void shouldGetTasks() throws IOException, InterruptedException {
        Task task2 = new Task("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10));
        manager.create(task);
        manager.create(task2);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertEquals(tasksFromManager, gson.fromJson(response.body(), new TaskListTypeToken().getType()));
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        Task task2 = new Task("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10));
        manager.create(task);
        manager.create(task2);

        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertEquals(task2, gson.fromJson(response.body(), Task.class));
    }

    @Test
    void shouldFailToGetTaskByWrongId() throws IOException, InterruptedException {
        manager.create(task);

        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("По переданному id ничего не найдено!", response.body());
    }

    @Test
    void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task2 = new Task("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10));
        manager.create(task);
        manager.create(task2);

        URI url = URI.create("http://localhost:8080/tasks/0");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
        assertEquals(1, tasksFromManager.getFirst().getId(), "Некорректный id");
    }

    static class TaskListTypeToken extends TypeToken<List<Task>> { }
}
