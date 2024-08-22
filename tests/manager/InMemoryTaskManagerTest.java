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
    private static Task task;
    private static Epic epic;
    private static SubTask subTask;

    @BeforeEach
    void initManager() {
        taskManager = new InMemoryTaskManager();
        task = new Task("задача", "я задача");
        epic = new Epic("эпик", "я эпик");
        subTask = new SubTask("задача", "я подзадача", epic);
    }

    @Test
    void shouldCreateTask() {
        taskManager.create(task);
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
        Epic savedEpic = taskManager.create(epic);

        assertEquals(1, taskManager.getEpics().size());
        assertEquals(0, savedEpic.getId());
        assertEquals(epic, taskManager.getEpicById(0));
    }

    @Test
    void shouldCreateSubTask() {
        taskManager.create(epic);
        SubTask savedSubTask = taskManager.create(subTask);

        assertEquals(1, taskManager.getSubTasks().size());
        assertEquals(1, savedSubTask.getId());
        assertEquals(subTask, taskManager.getSubTaskById(1));
    }

    @Test
    void shouldFailToCreateSubTaskWithNotExistingEpic() {
        SubTask savedSubTask = taskManager.create(subTask);

        assertEquals(0, taskManager.getSubTasks().size());
        assertNull(savedSubTask);
    }

    @Test
    void shouldNotCreateTaskWithSameId() {
        taskManager.create(task);
        Task anotherTask = taskManager.create(new Task("задача", "я задача", Status.NEW, 0));

        assertEquals(2, taskManager.getTasks().size());
        assertEquals(0, task.getId());
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
}
