package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {

    private static Epic epic;
    private static SubTask subTaskNew;
    private static SubTask subTaskDone;
    private static SubTask subTaskInProgress;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void initManager() {
        epic = new Epic("Эпик", "Описание", Status.NEW, 1, new ArrayList<>());
        subTaskNew = new SubTask("задача", "я подзадача", Status.NEW, 2, Duration.ofMinutes(60L), now, epic);
        subTaskDone = new SubTask("задача", "я подзадача", Status.DONE, 3, Duration.ofMinutes(60L), now, epic);
        subTaskInProgress = new SubTask("задача", "я подзадача", Status.IN_PROGRESS, 4, Duration.ofMinutes(60L), now, epic);
    }

    @Test
    void shouldEpicsBeEqualIfIdIsEqual() {
        Epic epic2 = new Epic("Задача2", "Описание2", Status.NEW, 1, new ArrayList<>());

        assertEquals(epic, epic2);
    }

    @Test
    void shouldSetStatusToNewIfAllSubtasksNew() {
        epic.addSubTask(subTaskNew);
        assertEquals(Status.NEW, epic.status);
    }

    @Test
    void shouldSetStatusToDoneIfAllSubtasksDone() {
        epic.addSubTask(subTaskDone);
        assertEquals(Status.DONE, epic.status);
    }

    @Test
    void shouldSetStatusToInProgressIfAllSubtasksInProgress() {
        epic.addSubTask(subTaskInProgress);
        assertEquals(Status.IN_PROGRESS, epic.status);
    }

    @Test
    void shouldSetStatusToInProgressIfAllSubtasksNewAndDone() {
        epic.addSubTask(subTaskDone);
        epic.addSubTask(subTaskNew);
        assertEquals(Status.IN_PROGRESS, epic.status);
    }
}