package tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {

    @Test
    void shouldTasksBeEqualIfIdIsEqual() {
        Task task1 = new Task("Задача", "Описание", Status.NEW, 1);
        Task task2 = new Task("Задача2", "Описание2", Status.DONE, 1);

        assertEquals(task1, task2);
    }
}
