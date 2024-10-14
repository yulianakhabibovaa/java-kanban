package tasks;

import static tasks.TaskType.*;

public class TaskUtils {
    public static String toString(Task task) {
        return String.format("%s,%s,%s,%s,%s,\n", task.getId(), TASK, task.getTitle(), task.getStatus(), task.getDescription());
    }

    public static String toString(SubTask task) {
        return String.format("%s,%s,%s,%s,%s,%s\n", task.getId(), SUBTASK, task.getTitle(), task.getStatus(), task.getDescription(), task.getCurrentEpic().getId());
    }

    public static String toString(Epic task) {
        return String.format("%s,%s,%s,%s,%s,\n", task.getId(), EPIC, task.getTitle(), task.getStatus(), task.getDescription());
    }
}
