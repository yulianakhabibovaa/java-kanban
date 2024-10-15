package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static tasks.TaskUtils.startDateToString;

public class Epic extends Task {

    private ArrayList<SubTask> subTasks;
    private LocalDateTime endTime;

    public Epic(Epic epic) {
        this(epic.title, epic.description, epic.status, epic.id, epic.subTasks, epic.duration, epic.startTime);
    }

    public Epic(String title, String description, Status status, int id, List<SubTask> subTasks, Duration duration, LocalDateTime startTime) {
        super(title, description, status, id, duration, startTime);
        this.subTasks = (ArrayList<SubTask>) subTasks;
        endTime = calculateEndTime();
    }

    public Epic(String title, String description, Status status, int id, List<SubTask> subTasks) {
        super(title, description, status, id);
        this.duration = calculateDuration();
        this.startTime = calculateStartTime();
        this.subTasks = (ArrayList<SubTask>) subTasks;
        endTime = calculateEndTime();
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

    private Duration calculateDuration() {
        Duration sum = Duration.ofSeconds(0);
        if (subTasks != null) {
            for (SubTask task : subTasks) {
                sum = sum.plus(task.duration);
            }
        }
        return sum;
    }

    private LocalDateTime calculateStartTime() {
        if (subTasks == null) {
            return null;
        }
        return subTasks.stream()
                .map(task -> task.startTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime calculateEndTime() {
        return startTime != null && duration != null ? startTime.plus(duration) : null;
    }

    private void calculateTimes() {
        duration = calculateDuration();
        startTime = calculateStartTime();
        endTime = calculateEndTime();
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
                ", startTime=" + startDateToString(this) +
                '}';
    }
}

