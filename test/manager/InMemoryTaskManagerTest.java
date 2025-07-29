package manager;

import model.*;
import org.junit.jupiter.api.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        manager = new InMemoryTaskManager(historyManager);
    }

    // Существующие тесты остаются без изменений
    @Test
    void createAndGetTask() {
        Task task = manager.createTask(new Task("Test", "Description"));
        assertNotNull(task.getId());
        assertEquals(task, manager.getTask(task.getId()));
    }

    @Test
    void createAndGetEpic() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        assertNotNull(epic.getId());
        assertEquals(epic, manager.getEpic(epic.getId()));
    }

    @Test
    void createAndGetSubtask() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));
        assertNotNull(subtask.getId());
        assertEquals(subtask, manager.getSubtask(subtask.getId()));
    }

    @Test
    void shouldNotCreateSubtaskWithoutEpic() {
        Subtask invalidSubtask = new Subtask("Subtask", "Description", 999);
        assertNull(manager.createSubtask(invalidSubtask));
    }


    @Test
    void getAllTasks() {
        Task task1 = manager.createTask(new Task("Task1", "Desc"));
        Task task2 = manager.createTask(new Task("Task2", "Desc"));
        assertEquals(2, manager.getAllTasks().size());
    }

    @Test
    void updateTask() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        Task updated = new Task("Updated", "New desc");
        updated.setId(task.getId());
        assertTrue(manager.updateTask(updated));
        assertEquals("Updated", manager.getTask(task.getId()).getTitle());
    }

    @Test
    void deleteTask() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        assertTrue(manager.deleteTask(task.getId()));
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void shouldAddToHistoryWhenGettingTask() {
        Task task = manager.createTask(new Task("Test", "Desc"));
        manager.getTask(task.getId());
        assertEquals(1, manager.getHistory().size());
        assertEquals(task, manager.getHistory().get(0));
    }

    @Test
    void shouldRemoveFromHistoryWhenDeletingTask() {
        Task task = manager.createTask(new Task("Test", "Desc"));
        manager.getTask(task.getId());
        manager.deleteTask(task.getId());
        assertEquals(0, manager.getHistory().size());
    }

    @Test
    void epicStatusShouldBeNewWithNoSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }


    @Test
    void shouldGenerateUniqueIds() {
        Task task1 = manager.createTask(new Task("Task1", "Desc"));
        Task task2 = manager.createTask(new Task("Task2", "Desc"));
        assertNotEquals(task1.getId(), task2.getId());
    }

    @Test
    void shouldNotUpdateTaskWithWrongId() {
        Task task = new Task("Task", "Desc");
        task.setId(999);
        assertFalse(manager.updateTask(task));
    }

    @Test
    void shouldDeleteAllTasksAndClearHistory() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        manager.getTask(task.getId());
        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());
    }

    // Новые тесты для статусов Epic
    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksNewAndDone() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenAnySubtaskInProgress() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    // Тесты для проверки пересечения интервалов
    @Test
    void shouldNotAllowOverlappingTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task1", "Desc");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofHours(1));
        manager.createTask(task1);

        Task task2 = new Task("Task2", "Desc");
        task2.setStartTime(now.plusMinutes(30));
        task2.setDuration(Duration.ofHours(1));

        assertNull(manager.createTask(task2));
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task1", "Desc");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofHours(1));
        manager.createTask(task1);

        Task task2 = new Task("Task2", "Desc");
        task2.setStartTime(now.plusHours(2));
        task2.setDuration(Duration.ofHours(1));

        assertNotNull(manager.createTask(task2));
    }

    // Тесты для HistoryManager
    @Test
    void shouldReturnEmptyHistoryWhenNoTasksViewed() {
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void shouldNotAddDuplicatesToHistory() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        manager.getTask(task.getId());
        manager.getTask(task.getId());
        assertEquals(1, manager.getHistory().size());
    }

    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        Task task = manager.createTask(new Task("Task", "Desc"));
        manager.getTask(task.getId());
        manager.deleteTask(task.getId());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void shouldRemoveEpicFromHistoryWhenDeleted() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.getEpic(epic.getId());
        manager.deleteEpic(epic.getId());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void shouldRemoveSubtaskFromHistoryWhenDeleted() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Sub", "Desc", epic.getId()));
        manager.getSubtask(subtask.getId());
        manager.deleteSubtask(subtask.getId());
        assertTrue(manager.getHistory().isEmpty());
    }

    // Тесты для временных характеристик Epic
    @Test
    void shouldCalculateEpicTimeWhenNoSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        assertNull(epic.getStartTime());
        assertEquals(Duration.ZERO, epic.getDuration()); // или assertNull(epic.getDuration())
        assertNull(epic.getEndTime());
    }

    @Test
    void shouldCalculateEpicTimeWithSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        LocalDateTime start = LocalDateTime.now();
        Subtask subtask1 = new Subtask("Sub1", "Desc", epic.getId());
        subtask1.setStartTime(start);
        subtask1.setDuration(Duration.ofHours(1));
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Sub2", "Desc", epic.getId());
        subtask2.setStartTime(start.plusHours(2));
        subtask2.setDuration(Duration.ofHours(1));
        manager.createSubtask(subtask2);

        assertEquals(start, epic.getStartTime());
        assertEquals(Duration.ofHours(2), epic.getDuration()); // Сумма 1h + 1h
        assertEquals(start.plusHours(3), epic.getEndTime()); // Последняя подзадача заканчивается через 3 часа
    }
}