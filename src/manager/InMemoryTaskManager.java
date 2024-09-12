package manager;

import history.HistoryManager;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int lastId;

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public void clearTasks() {
        tasks.clear();
    }

    @Override
    public void clearSubTasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubTasks();
        }
        subTasks.clear();
    }

    @Override
    public void clearEpics() {
        subTasks.clear();
        epics.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task result = tasks.get(id).copy();
        addToHistory(result);
        return result;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic result = epics.get(id).copy();
        addToHistory(result);
        return result;
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        SubTask result = subTasks.get(id).copy();
        addToHistory(result);
        return result;
    }

    @Override
    public void clearTaskById(Integer id) {
        tasks.remove(id);
    }

    @Override
    public void clearSubTaskById(Integer id) {
        SubTask subTask = subTasks.get(id);
        subTask.getCurrentEpic().clearSubTask(subTask);
        subTasks.remove(id);
    }

    @Override
    public void clearEpicById(Integer id) {
        ArrayList<SubTask> epicSubTasks = epics.get(id).getSubTasks();
        for (SubTask subTask : epicSubTasks) {
            subTasks.remove(subTask.getId());
        }
        epics.remove(id);
    }

    @Override
    public ArrayList<SubTask> getSubTasksByEpic(Epic epic) {
        return new ArrayList<>(epic.getSubTasks());
    }

    @Override
    public Task create(Task task) {
        if (task == null) {
            return null;
        }
        task.setId(lastId++);
        Task added = task.copy();
        tasks.put(task.getId(), added);
        return added.copy();
    }

    @Override
    public Task update(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            Task added = task.copy();
            tasks.put(task.getId(), added);
            return added.copy();
        }
        return null;
    }

    @Override
    public Epic create(Epic epic) {
        if (epic == null) {
            return null;
        }
        epic.setId(lastId++);
        Epic added = epic.copy();
        epics.put(epic.getId(), added);
        return added.copy();
    }

    @Override
    public Epic update(Epic epic) {
        if (epic != null && epics.containsKey(epic.getId())) {
            Epic added = epic.copy();
            epics.put(epic.getId(), added);
            return added.copy();
        }
        return null;
    }

    @Override
    public SubTask create(SubTask subTask) {
        if (subTask != null && epics.containsKey(subTask.getCurrentEpic().getId())) {
            subTask.setId(lastId++);
            epics.get(subTask.getCurrentEpic().getId()).addSubTask(subTask);
            SubTask added = subTask.copy();
            subTasks.put(subTask.getId(), added);
            return added.copy();
        }
        return null;
    }

    @Override
    public SubTask update(SubTask subTask) {
        if (subTask != null && subTasks.containsKey(subTask.getId()) && epics.containsKey(subTask.getCurrentEpic().getId())) {
            SubTask added = subTask.copy();
            Epic updatedEpic = subTask.getCurrentEpic();
            if (subTasks.get(subTask.getId()).getCurrentEpic() == subTask.getCurrentEpic()) {
                updatedEpic.updateSubTask(added);
            } else {
                subTasks.get(subTask.getId()).getCurrentEpic().clearSubTask(subTask);
                updatedEpic.addSubTask(added);
            }
            update(updatedEpic);
            subTasks.put(subTask.getId(), added);
            return added.copy();
        }
        return null;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void addToHistory(Task task) {
        historyManager.add(task);
    }
}
