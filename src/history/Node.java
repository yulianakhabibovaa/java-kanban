package history;

import tasks.Task;

public class Node {
    private Task task;
    private Node prev;
    private Node next;

    public Node(Task task, Node prev, Node next) {
        this.task = task;
        this.prev = prev;
        this.next = next;
    }

    public Node(Task task) {
        this.task = task;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void removePrev() {
        this.prev = null;
    }

    public void removeNext() {
        this.next = null;
    }

    public Task getTask() {
        return task;
    }

    public Node getPrev() {
        return prev;
    }

    public Node getNext() {
        return next;
    }
}
