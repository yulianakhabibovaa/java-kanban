package manager;

import exceptions.ManagerTimeCrossingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class TaskManagerTest<T extends TaskManager> {

    protected static Task task;
    protected static Epic epic;
    protected static SubTask subTask;
    protected LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void initManager() {
        task = new Task("задача", "я задача", Duration.ofMinutes(30L), now);
        epic = new Epic("эпик", "я эпик");
        subTask = new SubTask("задача", "я подзадача", epic, Duration.ofMinutes(30L), now.plusMinutes(30L));
    }

    protected abstract T createTaskManager();

    @Test
    void shouldGetTasks() {
        T taskManager = createTaskManager();
        taskManager.create(task);

        assertEquals(List.of(task), taskManager.getTasks());
    }

    @Test
    void shouldGetSubTasks() {
        T taskManager = createTaskManager();
        taskManager.create(epic);
        taskManager.create(subTask);

        assertEquals(List.of(subTask), taskManager.getSubTasks());
    }

    @Test
    void shouldGetEpics() {
        T taskManager = createTaskManager();
        taskManager.create(epic);

        assertEquals(List.of(epic), taskManager.getEpics());
    }

    @Test
    void shouldClearTasks() {
        T taskManager = createTaskManager();
        taskManager.create(task);
        taskManager.clearTasks();

        assertEquals(List.of(), taskManager.getTasks());
    }

    @Test
    void shouldClearSubTasks() {
        T taskManager = createTaskManager();
        taskManager.create(epic);
        taskManager.create(subTask);
        taskManager.clearSubTasks();

        assertEquals(List.of(), taskManager.getSubTasks());
    }

    @Test
    void shouldClearEpics() {
        T taskManager = createTaskManager();
        taskManager.create(epic);
        taskManager.create(subTask);
        taskManager.clearEpics();
        assertEquals(List.of(), taskManager.getEpics());
        assertEquals(List.of(), taskManager.getSubTasks());
    }

    @Test
    void shouldGetTaskById() {
        T taskManager = createTaskManager();
        taskManager.create(task);
        Task task2 = taskManager.create(new Task("task2", "description2", Duration.ofMinutes(10), now.plusMinutes(30)));

        assertEquals(task, taskManager.getTaskById(0));
        assertEquals(task2, taskManager.getTaskById(1));
    }

    @Test
    void shouldGetSubTaskById() {
        T taskManager = createTaskManager();
        taskManager.create(epic);
        taskManager.create(subTask);
        SubTask subTask2 = taskManager.create(new SubTask("subtask2", "description2", epic, Duration.ofMinutes(10), now.plusMinutes(60)));

        assertEquals(subTask, taskManager.getSubTaskById(1));
        assertEquals(subTask2, taskManager.getSubTaskById(2));
    }

    @Test
    void shouldGetEpicById() {
        T taskManager = createTaskManager();
        taskManager.create(epic);
        Epic epic2 = taskManager.create(new Epic("epic2", "description2"));

        assertEquals(epic, taskManager.getEpicById(0));
        assertEquals(epic2, taskManager.getEpicById(1));
    }

    @Test
    void shouldClearTaskById() {
        T taskManager = createTaskManager();
        taskManager.create(task);
        Task task2 = taskManager.create(new Task("task2", "description2", Duration.ofMinutes(10), now.plusMinutes(30)));
        taskManager.clearTaskById(0);

        assertEquals(1, taskManager.getTasks().size());
        assertEquals(task2, taskManager.getTaskById(1));
    }

    @Test
    void shouldClearSubTaskById() {
        T taskManager = createTaskManager();
        taskManager.create(epic);
        taskManager.create(subTask);
        SubTask subTask2 = taskManager.create(new SubTask("subtask2", "description2", epic, Duration.ofMinutes(10), now.plusMinutes(60)));
        taskManager.clearSubTaskById(1);

        assertEquals(1, taskManager.getSubTasks().size());
        assertEquals(subTask2, taskManager.getSubTaskById(2));
    }

    @Test
    void shouldClearEpicById() {
        T taskManager = createTaskManager();
        taskManager.create(epic);
        Epic epic2 = taskManager.create(new Epic("epic2", "description2"));
        taskManager.clearEpicById(0);

        assertEquals(1, taskManager.getEpics().size());
        assertEquals(epic2, taskManager.getEpicById(1));
    }

    @Test
    void shouldCreateTask() {
        T taskManager = createTaskManager();
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
        T taskManager = createTaskManager();
        Epic savedEpic = taskManager.create(epic);

        assertEquals(1, taskManager.getEpics().size());
        assertEquals(0, savedEpic.getId());
        assertEquals("эпик", savedEpic.getTitle(), "Title не совпал");
        assertEquals("я эпик", savedEpic.getDescription(), "Description не совпал");

    }

    @Test
    void shouldCreateSubTask() {
        T taskManager = createTaskManager();
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
        T taskManager = createTaskManager();
        SubTask savedSubTask = taskManager.create(subTask);

        assertEquals(0, taskManager.getSubTasks().size());
        assertNull(savedSubTask);
    }

    @Test
    void shouldNotCreateTaskWithSameId() {
        T taskManager = createTaskManager();
        Task savedTask = taskManager.create(task);
        Task anotherTask = taskManager.create(new Task("задача", "я задача", Status.NEW, 0, Duration.ofMinutes(30L), now.plusMinutes(60L)));

        assertEquals(2, taskManager.getTasks().size());
        assertEquals(0, savedTask.getId());
        assertEquals(1, anotherTask.getId());
        assertNotEquals(task, anotherTask);
    }

    @Test
    void shouldNotChangeTaskIfAddedToManager() {
        T taskManager = createTaskManager();
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
        T taskManager = createTaskManager();
        Epic savedEpic = taskManager.create(epic);
        SubTask savedSubTask = taskManager.create(subTask);

        assertEquals(savedSubTask, savedEpic.getSubTasks().getFirst(), "В эпике не сохранилась подзадача");

        taskManager.clearSubTaskById(savedSubTask.getId());

        assertEquals(0, taskManager.getSubTasks().size(), "Не удалилась подзадача");
        assertEquals(0, taskManager.getEpicById(savedEpic.getId()).getSubTasks().size(), "Не удалилась подзадача из эпика");
    }

    @Test
    void shouldGetSubTasksForEpic() {
        T taskManager = createTaskManager();
        Epic savedEpic = taskManager.create(epic);
        SubTask savedSubTask = taskManager.create(subTask);

        List<SubTask> epicsSubtasks = taskManager.getSubTasksByEpic(savedEpic);
        assertEquals(1, epicsSubtasks.size());
        assertEquals(savedSubTask, epicsSubtasks.getFirst());
    }

    @Test
    void shouldUpdateTask() {
        T taskManager = createTaskManager();
        taskManager.create(task);
        Task newTask = task.copy();
        newTask.setStatus(Status.DONE);
        newTask.setTitle("newTask");
        taskManager.update(newTask);

        assertEquals(1, taskManager.getTasks().size(), "Задача не найдена.");
        Task updatedTask = taskManager.getTaskById(0);
        assertEquals("newTask", updatedTask.getTitle(), "Title не совпал");
        assertEquals(Status.DONE, updatedTask.getStatus(), "Status не совпал");

    }

    @Test
    void shouldUpdateEpic() {
        T taskManager = createTaskManager();
        Epic savedEpic = taskManager.create(epic);
        Epic newEpic = savedEpic.copy();
        newEpic.setTitle("newEpic");
        newEpic.setDescription("eeeeeee");
        taskManager.update(newEpic);

        assertEquals(1, taskManager.getEpics().size());
        Epic updatedEpic = taskManager.getEpicById(0);
        assertEquals("newEpic", updatedEpic.getTitle(), "Title не совпал");
        assertEquals("eeeeeee", updatedEpic.getDescription(), "Description не совпал");
    }

    @Test
    void shouldUpdateSubTask() {
        T taskManager = createTaskManager();
        Epic savedEpic = taskManager.create(epic);
        SubTask savedSubTask = taskManager.create(subTask);
        SubTask newSubTask = savedSubTask.copy();
        newSubTask.setStatus(Status.DONE);
        newSubTask.setTitle("newSubtask");
        taskManager.update(newSubTask);

        assertEquals(1, taskManager.getSubTasks().size());
        SubTask updatedSubTask = taskManager.getSubTaskById(1);
        assertEquals("newSubtask", updatedSubTask.getTitle(), "Title не совпал");
        assertEquals("я подзадача", updatedSubTask.getDescription(), "Description не совпал");
        assertEquals(Status.DONE, updatedSubTask.getStatus(), "Status не совпал");
        assertEquals(savedEpic, taskManager.getSubTaskById(1).getCurrentEpic(), "Epic не совпадает");
        assertEquals(Status.DONE, taskManager.getEpicById(0).getStatus(), "Статус epic не изменился");
    }

    @Test
    void shouldGetPrioritizedTasks() {
        T taskManager = createTaskManager();
        Task task1 = new Task("задача 1", "я задача 1", Duration.ofMinutes(30L), now.plusMinutes(75L));
        Task task2 = new Task("задача 2", "я задача 2", Duration.ofMinutes(30L), null);
        SubTask subTask1 = new SubTask("задача", "я подзадача", epic, Duration.ofMinutes(30L), now.plusMinutes(30L));

        taskManager.create(task1);
        taskManager.create(epic);
        taskManager.create(subTask1);
        taskManager.create(task2);

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size());
        assertEquals(subTask1, prioritizedTasks.getFirst());
        assertEquals(task1, prioritizedTasks.getLast());
    }

    @Test
    void shouldFailToCreateIfTimeCrossing() {
        T taskManager = createTaskManager();
        Task task1 = new Task("задача 1", "я задача 1", Duration.ofMinutes(30L), now.plusMinutes(60L));
        Task task2 = new Task("задача 2", "я задача 2", Duration.ofMinutes(10L), now.plusMinutes(55L));
        Task task3 = new Task("задача 3", "я задача 3", Duration.ofMinutes(30L), now.plusMinutes(85L));
        Task task4 = new Task("задача 4", "я задача 4", Duration.ofMinutes(10L), now.plusMinutes(75L));
        Task task5 = new Task("задача 4", "я задача 4", Duration.ofMinutes(100L), now.plusMinutes(55L));

        taskManager.create(task1);
        assertThrows(ManagerTimeCrossingException.class, () -> taskManager.create(task1), "Полное совпадение недопустимо");
        assertThrows(ManagerTimeCrossingException.class, () -> taskManager.create(task2), "Пересечение в начале недопустимо");
        assertThrows(ManagerTimeCrossingException.class, () -> taskManager.create(task3), "Пересечение в конце недопустимо");
        assertThrows(ManagerTimeCrossingException.class, () -> taskManager.create(task4), "Пересечение в середине недопустимо");
        assertThrows(ManagerTimeCrossingException.class, () -> taskManager.create(task5), "Полное пересечение недопустимо");
    }
}
