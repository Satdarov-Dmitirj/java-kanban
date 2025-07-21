package manager;

import model.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> historyMap = new HashMap<>();

    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) return;

        int taskId = task.getId();


        if (historyMap.containsKey(taskId)) {
            removeNode(historyMap.get(taskId));
        }


        Node newNode = linkLast(task);
        historyMap.put(taskId, newNode);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.get(id);
        if (node != null) {
            removeNode(node);
            historyMap.remove(id);
        }
    }


    private Node linkLast(Task task) {
        Node newNode = new Node(tail, task, null);

        if (head == null) {
            // Список пуст: новый узел — и голова, и хвост
            head = newNode;
        } else {
            // Иначе добавляем после текущего хвоста
            tail.next = newNode;
        }


        tail = newNode;
        return newNode;
    }

    private void removeNode(Node node) {
        if (node == null) return;


        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            // Это был head
            head = node.next;
        }

        // Обновляем следующий элемент
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            // Это был tail
            tail = node.prev;
        }
    }

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }
}