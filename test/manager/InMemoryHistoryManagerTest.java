package manager;

import history.HistoryManager;
import history.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InMemoryHistoryManagerTest {

    private static HistoryManager historyManager;
    private static Task task;

    @BeforeEach
    void initManager() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("задача", "описание");
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
}
