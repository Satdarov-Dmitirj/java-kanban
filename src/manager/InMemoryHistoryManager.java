package manager;

import model.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 15;
    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    @Override
    public void add(Task task) {
        if (task == null) return;
        remove(task.getId());
        linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.remove(id);
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    private void linkLast(Task task) {
        if (historyMap.size() >= MAX_HISTORY_SIZE && head != null) {
            remove(head.task.getId());
        }

        Node newNode = new Node(task);
        if (tail == null) {
            head = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
        }
        tail = newNode;
        historyMap.put(task.getId(), newNode);
    }
}