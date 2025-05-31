package manager;

import model.Task;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private final HistoryManager manager = new InMemoryHistoryManager();

    @Test
    void shouldAddTasksAndKeepOnlyLast10() {
        for (int i = 1; i <= 10; i++) {
            Task task = new Task("Task " + i, "Desc");
            task.setId(i);
            manager.add(task);
        }

        assertEquals(10, manager.getHistory().size());
        assertEquals(6, manager.getHistory().get(0).getId());
        assertEquals(15, manager.getHistory().get(9).getId());
    }

    @Test
    void shouldNotAddNull() {
        manager.add(null);
        assertTrue(manager.getHistory().isEmpty());
    }
}