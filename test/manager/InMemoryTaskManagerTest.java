package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private static TaskManager taskManager;
    private static Task task;
    private static Epic epic;
    private static SubTask subTask;
    private LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void initManager() {
        taskManager = new InMemoryTaskManager();
        task = new Task("задача", "я задача", Duration.ofMinutes(30L), now.plusMinutes(30L));
        epic = new Epic("эпик", "я эпик");
        subTask = new SubTask("задача", "я подзадача", epic, Duration.ofMinutes(30L), now.plusMinutes(30L));
    }

    @Test
    void shouldCreateTask() {
        taskManager.create(task);

        assertEquals(1, taskManager.getTasks().size(), "Задача не найдена.");

        Task savedTask = taskManager.getTaskById(0);
        assertEquals("задача", savedTask.getTitle(), "Title не совпал");
        assertEquals("я задача", savedTask.getDescription(), "Description не совпал");


        List<Task> tasks = taskManager.getTasks();
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(savedTask, tasks.getFirst(), "Задачи не совпали");
    }

    @Test
    void shouldCreateEpic() {
        Epic savedEpic = taskManager.create(epic);

        assertEquals(1, taskManager.getEpics().size());
        assertEquals(0, savedEpic.getId());
        assertEquals("эпик", savedEpic.getTitle(), "Title не совпал");
        assertEquals("я эпик", savedEpic.getDescription(), "Description не совпал");

    }

    @Test
    void shouldCreateSubTask() {
        Epic savedEpic = taskManager.create(epic);
        SubTask savedSubTask = taskManager.create(subTask);

        assertEquals(1, taskManager.getSubTasks().size());
        assertEquals(1, savedSubTask.getId());
        assertEquals("задача", savedSubTask.getTitle(), "Title не совпал");
        assertEquals("я подзадача", savedSubTask.getDescription(), "Description не совпал");
        assertEquals(savedEpic, taskManager.getSubTaskById(1).getCurrentEpic(), "Epic не совпадает");
    }

    @Test
    void shouldFailToCreateSubTaskWithNotExistingEpic() {
        SubTask savedSubTask = taskManager.create(subTask);

        assertEquals(0, taskManager.getSubTasks().size());
        assertNull(savedSubTask);
    }

    @Test
    void shouldNotCreateTaskWithSameId() {
        Task savedTask = taskManager.create(task);
        Task anotherTask = taskManager.create(new Task("задача", "я задача", Status.NEW, 0, Duration.ofMinutes(30L), now.plusMinutes(30L)));

        assertEquals(2, taskManager.getTasks().size());
        assertEquals(0, savedTask.getId());
        assertEquals(1, anotherTask.getId());
        assertNotEquals(task, anotherTask);
    }

    @Test
    void shouldNotChangeTaskIfAddedToManager() {
        Task savedTask = taskManager.create(task);

        savedTask.setId(10);
        savedTask.setDescription("другое описание");
        savedTask.setStatus(Status.DONE);
        savedTask.setTitle("другая задача");

        Task check = taskManager.getTaskById(0);
        assertNotNull(check);
        assertEquals(0, check.getId());
        assertEquals("задача", check.getTitle());
        assertEquals("я задача", check.getDescription());
        assertEquals(Status.NEW, check.getStatus());
    }

    @Test
    void shouldRemoveDeletedSubtaskFromEpic() {
        Epic savedEpic = taskManager.create(epic);
        SubTask savedSubTask = taskManager.create(subTask);

        assertEquals(savedSubTask, savedEpic.getSubTasks().getFirst(), "В эпике не сохранилась подзадача");

        taskManager.clearSubTaskById(savedSubTask.getId());

        assertEquals(0, taskManager.getSubTasks().size(), "Не удалилась подзадача");
        assertEquals(0, taskManager.getEpicById(savedEpic.getId()).getSubTasks().size(), "Не удалилась подзадача из эпика");
    }
}
