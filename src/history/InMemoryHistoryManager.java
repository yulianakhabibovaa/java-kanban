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

        remove(task.getId());

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
            if (node.prev != null) {
                node.prev.next = node.next;
            } else {
                firstNode = node.next;
            }

            if (node.next != null) {
                node.next.prev = node.prev;
            } else {
                lastNode = node.prev;
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

        node.prev = lastNode;
        lastNode.next = node;
        lastNode = node;

    }

    private ArrayList<Task> getTasks() {
        Node node = firstNode;
        ArrayList<Task> result = new ArrayList<>();
        while (node != null) {
            result.add(node.task.copy());
            node = node.next;
        }

        return result;
    }

    private static class Node {
        private final Task task;
        private Node prev;
        private Node next;

        public Node(Task task) {
            this.task = task;
        }
    }

}
