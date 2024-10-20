package manager;

import history.HistoryManager;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class ManagersTest {

    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void shouldReturnWorkingDefaultTaskManager() {
        TaskManager taskManager = Managers.getDefault();

        Epic epic = taskManager.create(new Epic("эпик", "я эпик 1"));
        SubTask subTask = taskManager.create(new SubTask("подзадача", "я подзадача 1", epic, Duration.ofMinutes(30L), now.plusMinutes(30L)));
        Task task = taskManager.create(new Task("задача", "я  задача", Duration.ofMinutes(60L), now.plusMinutes(60L)));
        subTask.setStatus(Status.IN_PROGRESS);
        taskManager.update(subTask);

        assertIterableEquals(List.of(subTask), taskManager.getSubTasksByEpic(epic));
        assertEquals(0, epic.getId());
        assertEquals(1, subTask.getId());
        assertEquals(2, task.getId());
        assertEquals(1, taskManager.getEpics().size());
        assertEquals(1, taskManager.getSubTasks().size());
        assertEquals(1, taskManager.getTasks().size());
        assertEquals("эпик", taskManager.getEpicById(0).getTitle());
        assertEquals("подзадача", taskManager.getSubTaskById(1).getTitle());
        assertEquals("задача", taskManager.getTaskById(2).getTitle());
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(0).getStatus());
    }

    @Test
    void shouldReturnWorkingDefaultHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        Epic epic = new Epic("эпик", "я эпик 1", Status.NEW, 1, new ArrayList<>(), Duration.ZERO, null, null);
        SubTask subTask = new SubTask("подзадача", "я подзадача 1", Status.NEW, 2, Duration.ofMinutes(30L), now.plusMinutes(30L), epic);
        Task task = new Task("задача", "я  задача", Status.NEW, 3, Duration.ofMinutes(30L), now.plusMinutes(30L));

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subTask);

        assertEquals(3, historyManager.getHistory().size());
        assertEquals(List.of(task, epic, subTask), historyManager.getHistory());
    }
}
