package manager;

import model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);

    default void remove(int id) {
    }

    default List<Task> getHistory() {
        return null;
    }
}