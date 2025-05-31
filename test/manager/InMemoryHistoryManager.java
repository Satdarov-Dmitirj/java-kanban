package manager;

import manager.HistoryManager;
import manager.Managers;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        task2 = new Task("Task 2", "Description 2");
        task2.setId(2);
    }

    @Test
    void shouldAddTasksToHistory() {
        historyManager.add(task1);
        historyManager.add(task2);

        assertEquals(2, historyManager.getHistory().size(),
                "История должна содержать 2 задачи");
        assertEquals(task1, historyManager.getHistory().get(0),
                "Первая задача в истории должна быть task1");
        assertEquals(task2, historyManager.getHistory().get(1),
                "Вторая задача в истории должна быть task2");
    }

    @Test
    void shouldNotExceedLimitOf10Tasks() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Desc " + i);
            task.setId(i);
            historyManager.add(task);
        }

        assertEquals(10, historyManager.getHistory().size(),
                "История не должна превышать 10 задач");
        assertEquals(6, historyManager.getHistory().get(0).getId(),
                "Первая задача должна быть с ID 6 (последние 10 из 15)");
        assertEquals(15, historyManager.getHistory().get(9).getId(),
                "Последняя задача должна быть с ID 15");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId());

        assertEquals(1, historyManager.getHistory().size(),
                "История должна содержать 1 задачу после удаления");
        assertEquals(task2, historyManager.getHistory().get(0),
                "Оставшаяся задача должна быть task2");
    }

    @Test
    void shouldNotAddDuplicateTasks() {
        historyManager.add(task1);
        historyManager.add(task1);

        assertEquals(1, historyManager.getHistory().size(),
                "История должна содержать только 1 запись для дублирующихся задач");
    }

    @Test
    void shouldHandleEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(),
                "Новая история должна быть пустой");
    }
}