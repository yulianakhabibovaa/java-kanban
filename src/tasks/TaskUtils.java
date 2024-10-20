package tasks;

import java.time.LocalDateTime;

import static tasks.TaskType.*;

public class TaskUtils {
    public static String toString(Task task) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%n", task.getId(), TASK, task.getTitle(), task.getStatus(), task.getDescription(), task.getDuration().toMinutes(), dateToString(task.getStartTime()), dateToString(task.getEndTime()));
    }

    public static String toString(SubTask task) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s%n", task.getId(), SUBTASK, task.getTitle(), task.getStatus(), task.getDescription(), task.getDuration().toMinutes(), dateToString(task.getStartTime()), dateToString(task.getEndTime()), task.getCurrentEpic().getId());
    }

    public static String toString(Epic task) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%n", task.getId(), EPIC, task.getTitle(), task.getStatus(), task.getDescription(), task.getDuration().toMinutes(), dateToString(task.getStartTime()), dateToString(task.getEndTime()));
    }

    public static String dateToString(LocalDateTime time) {
        return time == null ? "null" : time.format(Task.DATE_TIME_FORMATER);
    }
}
