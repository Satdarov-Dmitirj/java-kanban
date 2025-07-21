package manager;

import model.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> history = new HashMap<>();
    private Node first;
    private Node last;

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) return;

        remove(task.getId());
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = history.remove(id);
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            first = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            last = node.prev;
        }
    }

    @Override
    public List<Task> getHistory() {
        return Optional.ofNullable(first)
                .map(this::getTasksFromNode)
                .orElse(Collections.emptyList());
    }

    private List<Task> getTasksFromNode(Node node) {
        List<Task> result = new ArrayList<>();
        while (node != null) {
            result.add(node.task);
            node = node.next;
        }
        return result;
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task, last, null);
        if (first == null) {
            first = newNode;
        } else {
            last.next = newNode;
        }
        last = newNode;
        history.put(task.getId(), newNode);
    }
}