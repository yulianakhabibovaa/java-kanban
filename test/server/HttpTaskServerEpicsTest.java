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

class HttpTaskServerEpicsTest {
    InMemoryTaskManager manager = new InMemoryTaskManager();
    HttpTaskServer server;
    HttpClient client = HttpClient.newHttpClient();
    LocalDateTime now = LocalDateTime.now();
    Epic epic;


    @BeforeEach
    void initServer() throws IOException {
        manager.clearEpics();
        manager.clearTasks();
        server = new HttpTaskServer(manager);
        epic = new Epic("Epic 1", "test epic");
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void shouldCreateEpic() throws IOException, InterruptedException {
        String taskJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 1", epicsFromManager.getFirst().getTitle(), "Некорректное имя эпика");
    }

    @Test
    void shouldUpdateEpic() throws IOException, InterruptedException {
        manager.create(epic);
        Epic update = epic.copy();
        update.setTitle("new title");
        String taskJson = gson.toJson(update);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("new title", epicsFromManager.getFirst().getTitle(), "Некорректное имя эпика");
    }

    @Test
    void shouldGetEpics() throws IOException, InterruptedException {
        Epic epic2 = new Epic("Epic 2", "Testing epic 2");
        manager.create(epic);
        manager.create(epic2);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertEquals(epicsFromManager, gson.fromJson(response.body(), new EpicListTypeToken().getType()));
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        Epic epic2 = new Epic("Epic 2", "Testing epic 2");
        manager.create(epic);
        manager.create(epic2);

        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertEquals(epic2, gson.fromJson(response.body(), Epic.class));
    }

    @Test
    void shouldFailToGetEpicByWrongId() throws IOException, InterruptedException {
        manager.create(epic);

        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("По переданному id ничего не найдено!", response.body());
    }

    @Test
    void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic2 = new Epic("Epic 2", "Testing epic 2");
        manager.create(epic);
        manager.create(epic2);

        URI url = URI.create("http://localhost:8080/epics/0");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 2", epicsFromManager.getFirst().getTitle(), "Некорректное имя эпика");
        assertEquals(1, epicsFromManager.getFirst().getId(), "Некорректный id");
    }

    @Test
    void shouldGetEpicSubTaskById() throws IOException, InterruptedException {
        manager.create(epic);
        SubTask subTask = new SubTask("Test 1", "Testing subtask 1", Status.NEW, null, Duration.ofMinutes(5), now, epic.getId());
        manager.create(subTask);

        URI url = URI.create("http://localhost:8080/epics/0/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertEquals(List.of(subTask), gson.fromJson(response.body(), new HttpTaskServerSubtasksTest.SubTaskListTypeToken().getType()));
    }

    @Test
    void shouldFailToGetSubTasksOfNotExistingEpic() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/1/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("По переданному id ничего не найдено!", response.body());
    }

    static class EpicListTypeToken extends TypeToken<List<Epic>> { }
}
