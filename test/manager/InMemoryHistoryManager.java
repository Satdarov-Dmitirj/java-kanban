package manager;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager manager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        manager = new InMemoryHistoryManager();
        task1 = new Task("Task 1", "Description");
        task1.setId(1);
        task2 = new Task("Task 2", "Description");
        task2.setId(2);
        task3 = new Task("Task 3", "Description");
        task3.setId(3);
    }

    @Test
    void shouldAddTasksToHistory() {
        manager.add(task1);
        manager.add(task2);
        assertEquals(List.of(task1, task2), manager.getHistory());
    }

    @Test
    void shouldRemoveDuplicates() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task1);
        assertEquals(List.of(task2, task1), manager.getHistory());
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);

        manager.remove(task2.getId());
        assertEquals(List.of(task1, task3), manager.getHistory());
    }

    @Test
    void shouldHandleEmptyHistory() {
        assertTrue(manager.getHistory().isEmpty());
        manager.remove(1);
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void shouldMaintainInsertionOrder() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);
        manager.add(task1);

        assertEquals(List.of(task2, task3, task1), manager.getHistory());
    }
}