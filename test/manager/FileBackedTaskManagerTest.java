package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static manager.FileBackedTaskManager.SCV_HEAD;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static File file;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void init() throws IOException {
        file = File.createTempFile("saveFile", ".csv");
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(file);
    }

    @AfterEach
    void clear() {
        file.delete();
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
        Files.writeString(saveFile.toPath(), "1,TASK,Task1,NEW,Description task1,30," + now.plusMinutes(30L).format(Task.DATE_TIME_FORMATER) + "," + now.plusMinutes(60L).format(Task.DATE_TIME_FORMATER) + ",\n", APPEND);
        Files.writeString(saveFile.toPath(), "2,EPIC,Epic2,DONE,Description epic2,60," + now.plusMinutes(60L).format(Task.DATE_TIME_FORMATER) + "," + now.plusMinutes(120L).format(Task.DATE_TIME_FORMATER) + ",\n", APPEND);
        Files.writeString(saveFile.toPath(), "3,SUBTASK,Sub Task2,DONE,Description sub task3,60," + now.plusMinutes(60L).format(Task.DATE_TIME_FORMATER) + "," + now.plusMinutes(120L).format(Task.DATE_TIME_FORMATER) + ",2\n", APPEND);
        assertEquals(4, Files.readAllLines(saveFile.toPath()).size());

        FileBackedTaskManager fileManager = new FileBackedTaskManager(saveFile);
        List<Task> prioritizedTasks = fileManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size());
        assertEquals("Task1", prioritizedTasks.getFirst().getTitle());
        assertEquals("Sub Task2", prioritizedTasks.getLast().getTitle());

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
        FileBackedTaskManager taskManager = createTaskManager();
        taskManager.create(task);
        taskManager.create(epic);
        taskManager.create(subTask);

        List<String> lines = Files.readAllLines(file.toPath());
        assertEquals(4, lines.size());
        assertEquals(SCV_HEAD.trim(), lines.getFirst());
        assertEquals("0,TASK,задача,NEW,я задача,30," + now.format(Task.DATE_TIME_FORMATER) + "," + now.plusMinutes(30L).format(Task.DATE_TIME_FORMATER) + ",", lines.get(1));
        assertEquals("1,EPIC,эпик,NEW,я эпик,30," + now.plusMinutes(30L).format(Task.DATE_TIME_FORMATER) + "," + now.plusMinutes(60L).format(Task.DATE_TIME_FORMATER) + ",", lines.get(2));
        assertEquals("2,SUBTASK,задача,NEW,я подзадача,30," + now.plusMinutes(30L).format(Task.DATE_TIME_FORMATER) + "," + now.plusMinutes(60L).format(Task.DATE_TIME_FORMATER) + ",1", lines.get(3));

        FileBackedTaskManager fileManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(taskManager.getTaskById(0), fileManager.getTasks().getFirst());
        assertEquals(taskManager.getSubTaskById(2), fileManager.getSubTasks().getFirst());
        assertEquals(taskManager.getEpicById(1), fileManager.getEpics().getFirst());
        assertEquals(taskManager.getEpicById(1).getSubTasks().getFirst(), fileManager.getEpics().getFirst().getSubTasks().getFirst());
    }

    @Test
    void shouldCorrectlyThrowExceptionsForFiles() {
        File incorrectFile = new File("incorrect");
        assertDoesNotThrow(() -> {
            new FileBackedTaskManager(incorrectFile);
        }, "Создание менеджера из несуществующего фала должно выдать ошибку");
    }


}
