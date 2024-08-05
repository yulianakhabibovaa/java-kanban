package tasks;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<SubTask> subTasks;

    public Epic(String title, String description, Status status, int id, ArrayList<SubTask> subTasks) {
        super(title, description, status, id);
        this.subTasks = subTasks;
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
        }
    }

    public void clearSubTask(SubTask subTask) {
        if (subTasks.contains(subTask)) {
            subTasks.remove(subTask);
            setEpicStatus();
        }
    }

    public void updateSubTask(SubTask subTask) {
        if (subTasks.contains(subTask)) {
            subTasks.remove(subTask);
            subTasks.add(subTask);
            setEpicStatus();
        }
    }

    public void clearSubTasks() {
        subTasks.clear();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subTasks=" + subTasks +
                ", title='" + title + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", description='" + description + '\'' +
                '}';
    }
}

