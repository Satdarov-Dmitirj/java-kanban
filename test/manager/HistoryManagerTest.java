
package manager;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {
    private HistoryManager manager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        manager = new InMemoryHistoryManager();
        task1 = new Task("Task1", "Description 1");
        task1.setId(1);
        task2 = new Task("Task2", "Description 2");
        task2.setId(2);
        task3 = new Task("Task3", "Description 3");
        task3.setId(3);
    }

    @Test
    void testEmptyHistory() {
        List<Task> history = manager.getHistory();
        assertNotNull(history, "Метод getHistory не должен возвращать null");
        assertTrue(history.isEmpty(), "История должна быть пустой при создании");
    }

    @Test
    void testAddTasks() {
        manager.add(task1);
        List<Task> history = manager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "Должен быть добавлен один элемент");

        manager.add(task2);
        assertEquals(2, manager.getHistory().size(), "Должны быть добавлены два элемента");
    }

    @Test
    void testDuplicateTasks() {
        manager.add(task1);
        manager.add(task1);

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "Дубликаты не должны добавляться в историю");
        assertEquals(task1, history.get(0), "Должен быть только один экземпляр задачи");
    }

    @Test
    void testRemoveFromStart() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);

        manager.remove(task1.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "После удаления первого элемента должно остаться 2");
        assertEquals(task2, history.get(0), "Первый элемент должен быть task2");
        assertEquals(task3, history.get(1), "Второй элемент должен быть task3");
    }

    @Test
    void testRemoveFromMiddle() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);

        manager.remove(task2.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "После удаления среднего элемента должно остаться 2");
        assertEquals(task1, history.get(0), "Первый элемент должен быть task1");
        assertEquals(task3, history.get(1), "Второй элемент должен быть task3");
    }

    @Test
    void testRemoveFromEnd() {
        manager.add(task1);
        manager.add(task2);

        manager.remove(task2.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "После удаления последнего элемента должно остаться 1");
        assertEquals(task1, history.get(0), "Должен остаться только task1");
    }

    @Test
    void testRemoveNonExisting() {
        manager.add(task1);

        manager.remove(9999); // несуществующий ID

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "Удаление несуществующего элемента не должно менять историю");
        assertEquals(task1, history.get(0), "Задача должна остаться в истории");
    }
}
