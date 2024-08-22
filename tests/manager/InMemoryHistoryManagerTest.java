package manager;

import history.HistoryManager;
import history.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeAll;
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
}
