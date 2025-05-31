package manager;

import model.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private final TaskManager manager = Managers.getDefault();

    @Test
    void shouldAddToHistoryWhenGettingTask() {
        Task task = manager.createTask(new Task("Test", "Desc"));
        manager.getTask(task.getId());

        assertEquals(1, manager.getHistory().size());
        assertEquals(task, manager.getHistory().get(0));
    }

    @Test
    void shouldNotRemoveFromHistoryWhenDeletingTask() {
        Task task = manager.createTask(new Task("Test", "Desc"));
        manager.getTask(task.getId());
        manager.deleteTask(task.getId());

        assertEquals(1, manager.getHistory().size()); // История сохраняется
    }
}