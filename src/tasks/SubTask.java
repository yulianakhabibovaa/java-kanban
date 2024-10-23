package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

import static tasks.TaskUtils.dateToString;

public class SubTask extends Task {
    private Epic currentEpic;

    public SubTask(SubTask subTask) {
        this(subTask.title, subTask.description, subTask.status, subTask.id, subTask.duration, subTask.startTime, subTask.currentEpic);
    }

    public SubTask(String title, String description, Status status, int id, Duration duration, LocalDateTime startTime, Epic currentEpic) {
        super(title, description, status, id, duration, startTime);
        this.currentEpic = currentEpic;
    }

    public SubTask(String title, String description, Epic currentEpic, Duration duration, LocalDateTime startTime) {
        super(title, description, duration, startTime);
        this.currentEpic = currentEpic;
    }

    public Epic getCurrentEpic() {
        return currentEpic;
    }

    @Override
    public SubTask copy() {
        return new SubTask(this);
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "currentEpic='" + currentEpic.title + '\'' +
                ", title='" + title + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", duration=" + duration.toMinutes() +
                ", startTime=" + dateToString(startTime) +
                ", endTime=" + dateToString(getEndTime()) +
                '}';
    }
}
