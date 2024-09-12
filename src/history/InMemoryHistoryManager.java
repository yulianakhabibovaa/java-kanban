package history;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {

    private final HashMap<Integer, Node> mapOfHistory = new HashMap<>();
    private Node lastNode;
    private Node firstNode;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (mapOfHistory.containsKey(task.getId())) {
            remove(task.getId());
        }
        Node newNode = new Node(task.copy());
        linkLast(newNode);
        mapOfHistory.put(task.getId(), newNode);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(getTasks());
    }

    @Override
    public void remove(int id) {
        if (mapOfHistory.containsKey(id)) {
            Node node = mapOfHistory.get(id);
            if (node.getPrev() != null) {
                node.getPrev().setNext(node.getNext());
            } else {
                firstNode = node.getNext();
            }

            if (node.getNext() != null) {
                node.getNext().setPrev(node.getPrev());
            } else {
                lastNode = node.getPrev();
            }

            mapOfHistory.remove(id);
        }
    }

    private void linkLast(Node node) {
        if (lastNode == null) {
            firstNode = node;
            lastNode = node;
            return;
        }

        node.setPrev(lastNode);
        lastNode.setNext(node);
        lastNode = node;

    }

    private ArrayList<Task> getTasks() {
        Node node = firstNode;
        ArrayList<Task> result = new ArrayList<>();
        while (node != null) {
            result.add(node.getTask().copy());
            node = node.getNext();
        }

        return result;
    }
}
