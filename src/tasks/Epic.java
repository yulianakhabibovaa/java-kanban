package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static tasks.TaskUtils.dateToString;

public class Epic extends Task {

    private final ArrayList<SubTask> subTasks;
    private LocalDateTime endTime;

    public Epic(Epic epic) {
        this(epic.title, epic.description, epic.status, epic.id, epic.subTasks, epic.duration, epic.startTime, epic.endTime);
    }

    public Epic(String title, String description, Status status, int id, List<SubTask> subTasks, Duration duration, LocalDateTime startTime, LocalDateTime endTime) {
        super(title, description, status, id, duration, startTime);
        this.subTasks = (ArrayList<SubTask>) subTasks;
        this.endTime = endTime == null ? null : endTime.truncatedTo(ChronoUnit.SECONDS);
    }

    public Epic(String title, String description) {
        super(title, description);
        this.subTasks = new ArrayList<>();
    }

    public ArrayList<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setEpicStatus() {
        if (subTasks.isEmpty()) {
            status = Status.NEW;
            return;
        }
        Status firstStatus = subTasks.getFirst().status;
        if (firstStatus == Status.IN_PROGRESS) {
            status = Status.IN_PROGRESS;
            return;
        }
        for (int i = 1; i < subTasks.size(); i++) {
            if (subTasks.get(i).status != firstStatus) {
                status = Status.IN_PROGRESS;
                return;
            }
        }
        status = firstStatus;
    }

    public void addSubTask(SubTask subTask) {
        if (!subTasks.contains(subTask)) {
            subTasks.add(subTask);
            setEpicStatus();
            calculateTimes();
        }
    }

    public void clearSubTask(SubTask subTask) {
        if (subTasks.contains(subTask)) {
            subTasks.remove(subTask);
            setEpicStatus();
            calculateTimes();
        }
    }

    public void updateSubTask(SubTask subTask) {
        if (subTasks.contains(subTask)) {
            subTasks.remove(subTask);
            subTasks.add(subTask);
            setEpicStatus();
            calculateTimes();
        }
    }

    public void clearSubTasks() {
        subTasks.clear();
        setEpicStatus();
        calculateTimes();
    }

    private void calculateTimes() {
        startTime = null;
        endTime = null;
        if (subTasks == null) {
            duration = Duration.ZERO;
        } else {
            subTasks.forEach(task -> {
                if (startTime == null || task.startTime.isBefore(startTime)) {
                    startTime = task.startTime;
                }
                duration = duration.plus(task.duration);
                if (endTime == null || task.getEndTime().isAfter(endTime)) {
                    endTime = task.getEndTime();
                }
            });
        }
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public Epic copy() {
        return new Epic(this);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subTasks=" + subTasks +
                ", title='" + title + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", duration=" + duration.toMinutes() +
                ", startTime=" + dateToString(this.startTime) +
                ", endTime=" + dateToString(this.endTime) +
                '}';
    }
}

