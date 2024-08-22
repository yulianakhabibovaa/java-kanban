package history;

import tasks.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

    private final ArrayList<Task> history = new ArrayList<>();
    private static final int MAX_HISTORY_CAPACITY = 10;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        if (history.size() == MAX_HISTORY_CAPACITY) {
            history.removeFirst();
        }

        history.addLast(task.copy());
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
