package manager;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;

public interface TaskManager {
    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<SubTask> getSubTasks();

    ArrayList<Task> getPrioritizedTasks();

    void clearTasks();

    void clearSubTasks();

    void clearEpics();

    Task getTaskById(Integer id);

    Epic getEpicById(Integer id);

    SubTask getSubTaskById(Integer id);

    void clearTaskById(Integer id);

    void clearSubTaskById(Integer id);

    void clearEpicById(Integer id);

    ArrayList<SubTask> getSubTasksByEpic(Epic epic);

    Task create(Task task);

    Task update(Task task);

    Epic create(Epic epic);

    Epic update(Epic epic);

    SubTask create(SubTask subTask);

    SubTask update(SubTask subTask);

    ArrayList<Task> getHistory();
}
