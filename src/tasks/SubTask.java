package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

import static tasks.TaskUtils.dateToString;

public class SubTask extends Task {
    private Integer currentEpic;

    public SubTask(SubTask subTask) {
        this(subTask.title, subTask.description, subTask.status, subTask.id, subTask.duration, subTask.startTime, subTask.currentEpic);
    }

    public SubTask(String title, String description, Status status, Integer id, Duration duration, LocalDateTime startTime, Integer currentEpic) {
        super(title, description, status, id, duration, startTime);
        this.currentEpic = currentEpic;
    }

    public SubTask(String title, String description, Integer currentEpic, Duration duration, LocalDateTime startTime) {
        super(title, description, duration, startTime);
        this.currentEpic = currentEpic;
    }

    public Integer getCurrentEpic() {
        return currentEpic;
    }

    public void setCurrentEpic(Epic epic) {
        this.currentEpic = epic.getId();
    }

    public void setCurrentEpic(Integer epicId) {
        this.currentEpic = epicId;
    }

    @Override
    public SubTask copy() {
        return new SubTask(this);
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "currentEpic=" + currentEpic +
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
