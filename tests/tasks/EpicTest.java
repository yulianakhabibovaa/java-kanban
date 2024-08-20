package tasks;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void shouldEpicsBeEqualIfIdIsEqual() {
        Epic task1 = new Epic("Эпик", "Описание", Status.NEW, 1, new ArrayList<>());
        Epic task2 = new Epic("Задача2", "Описание2", Status.NEW, 1, new ArrayList<>());

        assertEquals(task1, task2);
    }
}