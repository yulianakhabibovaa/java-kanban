package manager;

import exceptions.ManagerTimeCrossingException;
import history.HistoryManager;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final Comparator<Task> priorityComparator = Comparator.comparing(Task::getStartTime);
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected TreeSet<Task> prioritisedTasks = new TreeSet<>(priorityComparator);

    protected int lastId;

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
    public ArrayList<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritisedTasks);
    }

    @Override
    public void clearTasks() {
        clearTasksFromPrioritized();
        tasks.clear();
    }

    @Override
    public void clearSubTasks() {
        clearSubTasksFromPrioritized();
        epics.values().forEach(Epic::clearSubTasks);
        subTasks.clear();
    }

    @Override
    public void clearEpics() {
        clearSubTasksFromPrioritized();
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
        prioritisedTasks.remove(tasks.get(id));
        tasks.remove(id);
    }

    @Override
    public void clearSubTaskById(Integer id) {
        SubTask subTask = subTasks.get(id);
        prioritisedTasks.remove(subTask);
        subTask.getCurrentEpic().clearSubTask(subTask);
        subTasks.remove(id);
    }

    @Override
    public void clearEpicById(Integer id) {
        epics.get(id).getSubTasks().forEach(subTask -> {
            prioritisedTasks.remove(subTask);
            subTasks.remove(subTask.getId());
        });
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
        validateTime(task);
        task.setId(lastId++);
        Task added = task.copy();
        tasks.put(task.getId(), added);
        if (added.getStartTime() != null) {
            prioritisedTasks.add(added.copy());
        }
        return added.copy();
    }

    @Override
    public Task update(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            prioritisedTasks.remove(task);
            validateTime(task);
            Task added = task.copy();
            tasks.put(task.getId(), added);
            if (added.getStartTime() != null) {
                prioritisedTasks.add(added.copy());
            }
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
            validateTime(subTask);
            subTask.setId(lastId++);
            epics.get(subTask.getCurrentEpic().getId()).addSubTask(subTask);
            SubTask added = subTask.copy();
            subTasks.put(subTask.getId(), added);
            if (added.getStartTime() != null) {
                prioritisedTasks.add(added.copy());
            }
            return added.copy();
        }
        return null;
    }

    @Override
    public SubTask update(SubTask subTask) {
        if (subTask != null && subTasks.containsKey(subTask.getId()) && epics.containsKey(subTask.getCurrentEpic().getId())) {
            prioritisedTasks.remove(subTask);
            validateTime(subTask);
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
            if (added.getStartTime() != null) {
                prioritisedTasks.add(added.copy());
            }
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

    private void clearTasksFromPrioritized() {
        prioritisedTasks = prioritisedTasks.stream().filter(task -> !tasks.containsValue(task)).collect(Collectors.toCollection(() -> new TreeSet<>(priorityComparator)));
    }

    private void clearSubTasksFromPrioritized() {
        prioritisedTasks = prioritisedTasks.stream().filter(task -> !subTasks.containsValue(task)).collect(Collectors.toCollection(() -> new TreeSet<>(priorityComparator)));
    }

    private void validateTime(Task task) {
        if (task.getStartTime() != null && getPrioritizedTasks().stream().anyMatch(it -> isTimeCrossing(it, task))) {
            throw new ManagerTimeCrossingException("Задача пересекается по времени с одной из существующих");
        }
    }

    private boolean isTimeCrossing(Task task1, Task task2) {
        LocalDateTime task1End = task1.getEndTime();
        LocalDateTime task2End = task2.getEndTime();
        return task1.getStartTime().isBefore(task2End) && task1End.isAfter(task2.getStartTime());
    }
}
