package tasks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubTaskTest {

    @Test
    void shouldSubTasksBeEqualIfIdIsEqual() {
        SubTask task1 = new SubTask("Задача", "Описание", Status.NEW, 1, new Epic("эпик", "я эпик"));
        SubTask task2 = new SubTask("Задача2", "Описание2", Status.DONE, 1, new Epic("эпик", "я эпик"));

        Assertions.assertEquals(task1, task2);
    }
}
