package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private static TaskManager taskManager;

    @BeforeEach
    void init() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateTask() {
        Task task = taskManager.create(new Task("Test addNewTask", "Test addNewTask description"));
        Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void shouldCreateEpic() {
        Epic epic = taskManager.create(new Epic("эпик", "я эпик"));

        assertEquals(1, taskManager.getEpics().size());
        assertEquals(0, epic.getId());
        assertEquals(epic, taskManager.getEpicById(0));
    }

    @Test
    void shouldCreateSubTask() {
        Epic epic = taskManager.create(new Epic("эпик", "я эпик"));
        SubTask subTask = taskManager.create(new SubTask("задача", "я подзадача", epic));

        assertEquals(1, taskManager.getSubTasks().size());
        assertEquals(1, subTask.getId());
        assertEquals(subTask, taskManager.getSubTaskById(1));
    }

    @Test
    void shouldFailToCreateSubTaskWithNotExistingEpic() {
        SubTask subTask = taskManager.create(new SubTask("задача", "я подзадача", new Epic("эпик", "я эпик")));

        assertEquals(0, taskManager.getSubTasks().size());
        assertNull(subTask);
    }

    @Test
    void shouldNotCreateTaskWithSameId() {
        Task task = taskManager.create(new Task("задача", "я задача"));
        Task anotherTask = taskManager.create(new Task("задача", "я задача", Status.NEW, 0));

        assertEquals(2, taskManager.getTasks().size());
        assertEquals(0, task.getId());
        assertEquals(1, anotherTask.getId());
        assertNotEquals(task, anotherTask);
    }

    @Test
    void shouldNotChangeTaskIfAddedToManager() {
        Task task = taskManager.create(new Task("задача", "я задача"));

        task.setId(10);
        task.setDescription("другое описание");
        task.setStatus(Status.DONE);
        task.setTitle("другая задача");

        Task check = taskManager.getTaskById(0);
        assertNotNull(check);
        assertEquals(0, check.getId());
        assertEquals("задача", check.getTitle());
        assertEquals("я задача", check.getDescription());
        assertEquals(Status.NEW, check.getStatus());
    }
}
