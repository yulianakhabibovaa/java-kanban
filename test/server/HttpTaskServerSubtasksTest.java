package server;

import com.google.gson.reflect.TypeToken;
import manager.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;

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

class HttpTaskServerSubtasksTest {

    InMemoryTaskManager manager;
    HttpTaskServer server;
    HttpClient client = HttpClient.newHttpClient();
    LocalDateTime now = LocalDateTime.now();
    Epic epic;
    SubTask subTask;

    @BeforeEach
    void initServer() throws IOException {
        manager = new InMemoryTaskManager();
        epic = manager.create(new Epic("Epic 1", "test epic"));
        subTask = new SubTask("Test 1", "Testing subtask 1", Status.NEW, null, Duration.ofMinutes(5), now, epic.getId());
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void shouldCreateSubTask() throws IOException, InterruptedException {
        String subtaskJson = gson.toJson(subTask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTasks();

        assertNotNull(subTasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subTasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Test 1", subTasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
        assertEquals(1, manager.getSubTasksByEpic(epic.getId()).size(), "Некорректное количество подзадач у эпика");
        assertEquals(1, manager.getPrioritizedTasks().size());
    }

    @Test
    void shouldFailToCreateSubTaskIfTimeCrossing() throws IOException, InterruptedException {
        manager.create(subTask);
        SubTask subTask2 = new SubTask("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now, epic.getId());
        String taskJson = gson.toJson(subTask2);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается по времени с существующими!", response.body());
    }

    @Test
    void shouldUpdateSubTask() throws IOException, InterruptedException {
        manager.create(subTask);
        SubTask update = subTask.copy();
        update.setStatus(Status.IN_PROGRESS);
        update.setTitle("new title");
        String taskJson = gson.toJson(update);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTasks();

        assertNotNull(subTasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subTasksFromManager.size(), "Некорректное количество задач");
        assertEquals("new title", subTasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
        assertEquals(Status.IN_PROGRESS, subTasksFromManager.getFirst().getStatus(), "Некорректный статус");
    }

    @Test
    void shouldFailToUpdateSubTaskIfTimeCrossing() throws IOException, InterruptedException {
        manager.create(subTask);
        SubTask subTask2 = manager.create(new SubTask("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10), epic.getId()));
        SubTask update = subTask2.copy();
        update.setStartTime(now);
        String taskJson = gson.toJson(update);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается по времени с существующими!", response.body());
    }

    @Test
    void shouldGetSubTasks() throws IOException, InterruptedException {
        SubTask subTask2 = new SubTask("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10), epic.getId());
        manager.create(subTask);
        manager.create(subTask2);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTasks();
        assertEquals(subTasksFromManager, gson.fromJson(response.body(), new SubTaskListTypeToken().getType()));
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        SubTask subTask2 = new SubTask("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10), epic.getId());
        manager.create(subTask);
        manager.create(subTask2);

        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertEquals(subTask2, gson.fromJson(response.body(), SubTask.class));
    }

    @Test
    void shouldFailToGetTaskByWrongId() throws IOException, InterruptedException {
        manager.create(subTask);

        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("По переданному id ничего не найдено!", response.body());
    }

    @Test
    void shouldDeleteTaskById() throws IOException, InterruptedException {
        SubTask subTask2 = new SubTask("Test 2", "Testing task 2", Status.NEW, null, Duration.ofMinutes(5), now.plusMinutes(10), epic.getId());
        manager.create(subTask);
        manager.create(subTask2);

        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTasks();

        assertNotNull(subTasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subTasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", subTasksFromManager.getFirst().getTitle(), "Некорректное имя задачи");
        assertEquals(2, subTasksFromManager.getFirst().getId(), "Некорректный id");
    }

    static class SubTaskListTypeToken extends TypeToken<List<SubTask>> { }
}
