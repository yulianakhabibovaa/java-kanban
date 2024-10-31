package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static tasks.TaskUtils.dateToString;

public class Task {

    public static final DateTimeFormatter DATE_TIME_FORMATER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    protected String title;
    protected Integer id;
    protected Status status;
    protected String description;
    protected Duration duration = Duration.ZERO;
    protected LocalDateTime startTime;

    public Task(Task task) {
        this(task.title, task.description, task.status, task.id, task.duration, task.startTime);
    }

    protected Task(String title, String description, Status status, Integer id) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.id = id;
    }

    public Task(String title, String description, Status status, Integer id, Duration duration, LocalDateTime startTime) {
        this(title, description, status, id);
        this.duration = duration;
        this.startTime = startTime == null ? null : startTime.truncatedTo(ChronoUnit.SECONDS);
    }

    protected Task(String title, String description) {
        this(title, description, Status.NEW, null);
    }

    public Task(String title, String description, Duration duration, LocalDateTime startTime) {
        this(title, description, Status.NEW, null, duration, startTime);
    }

    public String getTitle() {
        return title;
    }

    public Integer getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Task copy() {
        return new Task(this);
    }

    public LocalDateTime getEndTime() {
        return startTime == null ? null : startTime.plus(getDuration());
    }

    public Duration getDuration() {
        return duration == null ? Duration.ZERO : duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(getId(), task.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", duration=" + duration.toMinutes() +
                ", startTime=" + dateToString(startTime) +
                ", endTime=" + dateToString(getEndTime()) +
                '}';
    }
}
