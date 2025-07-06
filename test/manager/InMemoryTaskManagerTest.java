package manager;

import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        manager = new InMemoryTaskManager(historyManager);
    }

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
        assertThrows(IllegalArgumentException.class, () -> {
            manager.createSubtask(new Subtask("Subtask", "Description", 999));
        });
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
    void shouldNotRemoveFromHistoryWhenDeletingTask() {
        Task task = manager.createTask(new Task("Test", "Desc"));
        manager.getTask(task.getId());
        manager.deleteTask(task.getId());
        assertEquals(1, manager.getHistory().size());
    }

    @Test
    void epicStatusShouldBeNewWithNoSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Sub", "Desc", epic.getId()));
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }
}