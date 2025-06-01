package manager;

import model.Task;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Test", "Desc");
        task.setId(1);
        manager.add(task);
        assertEquals(1, manager.getHistory().size());
    }

    @Test
    void shouldNotAddNull() {
        manager.add(null);
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void shouldKeepLast10Tasks() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Desc");
            task.setId(i);
            manager.add(task);
        }
        assertEquals(10, manager.getHistory().size());
        assertEquals(6, manager.getHistory().get(0).getId());
        assertEquals(15, manager.getHistory().get(9).getId());
    }

    @Test
    void shouldRemoveDuplicates() {
        Task task = new Task("Task", "Desc");
        task.setId(1);
        manager.add(task);
        manager.add(task);
        assertEquals(1, manager.getHistory().size());
    }
}