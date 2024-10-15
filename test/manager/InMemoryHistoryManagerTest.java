package manager;

import history.HistoryManager;
import history.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InMemoryHistoryManagerTest {

    private static HistoryManager historyManager;
    private static Task task;
    private static Task taskTwo;
    private static Epic taskThree;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void initManager() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("задача", "описание", Duration.ofMinutes(30L), now.plusMinutes(30L));
        taskTwo = new Task("задача2", "описание2", Status.NEW,  1, Duration.ofMinutes(60L), now.plusMinutes(60L));
        taskThree = new Epic("эпик1", "описание3", Status.NEW, 2, new ArrayList<>());
    }

    @Test
    void shouldAddHistory() {
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    void shouldSavePreviousVersionInHistory() {
        historyManager.add(task);

        task.setStatus(Status.DONE);
        List<Task> history = historyManager.getHistory();

        assertEquals(Status.NEW, history.getFirst().getStatus(), "Статус у истории остался неизменным");
    }

    @Test
    void shouldReplaceOldTaskInHistory() {
        historyManager.add(task);

        task.setStatus(Status.DONE);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "В истории только 1 запись");
        assertEquals(Status.DONE, history.getFirst().getStatus(), "Статус у истории изменяется");
    }

    @Test
    void shouldRemoveTask() {
        historyManager.add(task);

        historyManager.remove(task.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(0, history.size(), "В истории не должно быть записей");
    }

    @Test
    void shouldRemoveTaskFromBeginning() {
        historyManager.add(task);
        historyManager.add(taskTwo);
        historyManager.add(taskThree);

        historyManager.remove(task.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "В истории должно быть 2 записи");
        assertEquals(1, history.getFirst().getId(), "Первой по порядку станет вторая задача");
        assertEquals(2, history.get(1).getId(), "Второй по порядку станет третья задача");
    }

    @Test
    void shouldRemoveTaskFromMiddle() {
        historyManager.add(task);
        historyManager.add(taskTwo);
        historyManager.add(taskThree);

        historyManager.remove(taskTwo.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "В истории должно быть 2 записи");
        assertEquals(0, history.getFirst().getId(), "Первой по порядку станет вторая задача");
        assertEquals(2, history.get(1).getId(), "Второй по порядку станет третья задача");
    }

    @Test
    void shouldRemoveTaskFromEnding() {
        historyManager.add(task);
        historyManager.add(taskTwo);
        historyManager.add(taskThree);

        historyManager.remove(taskThree.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "В истории должно быть 2 записи");
        assertEquals(0, history.getFirst().getId(), "Первой по порядку станет вторая задача");
        assertEquals(1, history.get(1).getId(), "Второй по порядку станет третья задача");
    }
}
