package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static manager.FileBackedTaskManager.SCV_HEAD;
import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private static FileBackedTaskManager taskManager;
    private static Task task;
    private static Epic epic;
    private static SubTask subTask;
    private static File file;

    @BeforeEach
    void initManager() throws IOException {
        file = File.createTempFile("saveFile", ".csv");
        taskManager = new FileBackedTaskManager(file);
        task = new Task("задача", "я задача");
        epic = new Epic("эпик", "я эпик");
        subTask = new SubTask("задача", "я подзадача", epic);
    }

    @AfterEach
    void clear() {
        file.delete();
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
        Task anotherTask = taskManager.create(new Task("задача", "я задача", Status.NEW, 0));

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

    @Test
    void shouldCreateTaskManagerFromEmptyFile() throws IOException {
        File emptyFile = File.createTempFile("empty", ".scv");
        FileBackedTaskManager fileManager = new FileBackedTaskManager(emptyFile);
        assertEquals(0, Files.size(emptyFile.toPath()));
        assertEquals(0, fileManager.getTasks().size());
        fileManager.clearTasks();
        assertEquals(1, Files.readAllLines(emptyFile.toPath()).size());
        assertEquals(SCV_HEAD, Files.readString(emptyFile.toPath()));
        emptyFile.delete();
    }

    @Test
    void shouldCreateTaskManagerFromNotEmptySaveFile() throws IOException {
        File saveFile = File.createTempFile("save", ".scv");
        Files.writeString(saveFile.toPath(), SCV_HEAD, TRUNCATE_EXISTING);
        Files.writeString(saveFile.toPath(), "1,TASK,Task1,NEW,Description task1,\n", APPEND);
        Files.writeString(saveFile.toPath(), "2,EPIC,Epic2,DONE,Description epic2,\n", APPEND);
        Files.writeString(saveFile.toPath(), "3,SUBTASK,Sub Task2,DONE,Description sub task3,2\n", APPEND);
        assertEquals(4, Files.readAllLines(saveFile.toPath()).size());

        FileBackedTaskManager fileManager = new FileBackedTaskManager(saveFile);
        Task savedTask = fileManager.create(task);

        assertEquals(2, fileManager.getTasks().size());
        assertEquals(1, fileManager.getSubTasks().size());
        assertEquals(1, fileManager.getEpics().size());
        assertEquals(4, savedTask.getId());
        assertEquals(2, fileManager.getEpics().getFirst().getId());
        assertEquals(3, fileManager.getSubTasks().getFirst().getId());
        assertEquals(1, fileManager.getTasks().getFirst().getId());
        assertEquals(fileManager.getSubTaskById(3), fileManager.getEpicById(2).getSubTasks().getFirst());


        saveFile.delete();
    }

    @Test
    void shouldSaveTasksToFileAndLoadFromIt() throws IOException {
        taskManager.create(task);
        taskManager.create(epic);
        taskManager.create(subTask);

        List<String> lines = Files.readAllLines(file.toPath());
        assertEquals(4, lines.size());
        assertEquals(SCV_HEAD.trim(), lines.getFirst());
        assertEquals("0,TASK,задача,NEW,я задача,", lines.get(1));
        assertEquals("1,EPIC,эпик,NEW,я эпик,", lines.get(2));
        assertEquals("2,SUBTASK,задача,NEW,я подзадача,1", lines.get(3));

        FileBackedTaskManager fileManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(taskManager.getTaskById(0), fileManager.getTasks().getFirst());
        assertEquals(taskManager.getSubTaskById(2), fileManager.getSubTasks().getFirst());
        assertEquals(taskManager.getEpicById(1), fileManager.getEpics().getFirst());
        assertEquals(taskManager.getEpicById(1).getSubTasks().getFirst(), fileManager.getEpics().getFirst().getSubTasks().getFirst());
    }
}
