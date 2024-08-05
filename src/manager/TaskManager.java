package manager;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();

    private int lastId;

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearSubTasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubTasks();
        }
        subTasks.clear();
    }

    public void clearEpics() {
        subTasks.clear();
        epics.clear();
    }

    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    public Task getEpicById(Integer id) {
        return epics.get(id);
    }

    public Task getSubTaskById(Integer id) {
        return subTasks.get(id);
    }

    public void clearTaskById(Integer id) {
        tasks.remove(id);
    }

    public void clearSubTaskById(Integer id) {
        SubTask subTask = subTasks.get(id);
        subTask.getCurrentEpic().clearSubTask(subTask);
        subTasks.remove(id);
    }

    public void clearEpicById(Integer id) {
        ArrayList<SubTask> epicSubTasks = epics.get(id).getSubTasks();
        for (SubTask subTask : epicSubTasks) {
            subTasks.remove(subTask.getId());
        }
        epics.remove(id);
    }

    public ArrayList<SubTask> getSubTasksByEpic(Epic epic) {
        return epic.getSubTasks();
    }

    public Task create(Task task) {
        task.setId(lastId++);
        tasks.put(task.getId(), task);
        return task;
    }

    public Task update(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            return task;
        }
        return null;
    }

    public Epic create(Epic epic) {
        epic.setId(lastId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Epic update(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            return epic;
        }
        return null;
    }

    public SubTask create(SubTask subTask) {
        if (epics.containsKey(subTask.getCurrentEpic().getId())) {
            subTask.setId(lastId++);
            subTask.getCurrentEpic().addSubTask(subTask);
            subTasks.put(subTask.getId(), subTask);
            return subTask;
        }
        return null;
    }

    public SubTask update(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId()) && epics.containsKey(subTask.getCurrentEpic().getId())) {
            if (subTasks.get(subTask.getId()).getCurrentEpic() == subTask.getCurrentEpic()) {
                subTask.getCurrentEpic().updateSubTask(subTask);
            } else {
                subTasks.get(subTask.getId()).getCurrentEpic().clearSubTask(subTask);
                subTask.getCurrentEpic().addSubTask(subTask);
            }
            subTasks.put(subTask.getId(), subTask);
            return subTask;
        }
        return null;
    }
}
