package manager;

import model.Task;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final LinkedList<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        // Удаляем дубликаты
        history.remove(task);

        // Добавляем в конец
        history.addLast(task);

        // Удаляем лишнее
        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history); // Возвращаем копию
    }
}