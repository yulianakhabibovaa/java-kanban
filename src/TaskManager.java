import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TaskManager {
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    HashMap<Integer, SubTask> subTasks = new HashMap<>();

    private int lastId;

    public Collection<Task> getTasks() {
        return tasks.values();
    }

    public Collection<Epic> getEpics() {
        return epics.values();
    }

    public Collection<SubTask> getSubTasks() {
        return subTasks.values();
    }

    public void clearTasks() {
        tasks = new HashMap<>();
    }

    public void clearSubTasks() {
        for (int id : subTasks.keySet()) {
            clearTaskById(id);
        }
    }

    public void clearEpics() {
        for (int id : epics.keySet()) {
            clearTaskById(id);
        }
    }

    public Task getTaskById(Integer id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else if (epics.containsKey(id)) {
            return epics.get(id);
        } else if (subTasks.containsKey(id)) {
            return subTasks.get(id);
        }
        return null;
    }

    public void clearTaskById(Integer id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        } else if (subTasks.containsKey(id)) {
            SubTask subTask = subTasks.get(id);
            subTask.getCurrentEpic().cleanSubTask(subTask);
            subTasks.remove(id);
        } else if (epics.containsKey(id)) {
            ArrayList<SubTask> epicSubTasks = epics.get(id).getSubTasks();
            for (SubTask subTask : epicSubTasks) {
                subTasks.remove(subTask.getId());
            }
            epics.remove(id);
        }
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
                subTasks.get(subTask.getId()).getCurrentEpic().cleanSubTask(subTask);
                subTask.getCurrentEpic().addSubTask(subTask);
            }
            subTasks.put(subTask.getId(), subTask);
            return subTask;
        }
        return null;
    }
}
