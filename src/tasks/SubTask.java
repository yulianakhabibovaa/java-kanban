package tasks;

public class SubTask extends Task {
    private Epic currentEpic;

    public SubTask(SubTask subTask) {
        this(subTask.title, subTask.description, subTask.status, subTask.id, subTask.currentEpic);
    }

    public SubTask(String title, String description, Status status, int id, Epic currentEpic) {
        super(title, description, status, id);
        this.currentEpic = currentEpic;
    }

    public SubTask(String title, String description, Epic currentEpic) {
        super(title, description);
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
                '}';
    }
}
