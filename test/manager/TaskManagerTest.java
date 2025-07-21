package manager;

import model.*;
import org.junit.jupiter.api.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected HistoryManager historyManager;
    protected Task task;
    protected Epic epic;
    protected Subtask subtask;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        taskManager = createTaskManager();

        task = new Task("Test Task", "Description");
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));

        epic = new Epic("Test Epic", "Description");

        subtask = new Subtask("Test Subtask", "Description", 2);
        subtask.setStartTime(LocalDateTime.now().plusHours(2));
        subtask.setDuration(Duration.ofHours(1));
    }

    protected abstract T createTaskManager();

    @Test
    void shouldCreateAndGetTask() {
        Task createdTask = taskManager.createTask(task);
        assertNotNull(createdTask.getId());
        assertEquals(createdTask, taskManager.getTask(createdTask.getId()));
    }

    @Test
    void shouldCreateAndGetEpic() {
        Epic createdEpic = taskManager.createEpic(epic);
        assertNotNull(createdEpic.getId());
        assertEquals(createdEpic, taskManager.getEpic(createdEpic.getId()));
    }

    @Test
    void shouldCreateAndGetSubtask() {
        Epic createdEpic = taskManager.createEpic(epic);
        Subtask createdSubtask = taskManager.createSubtask(
                new Subtask("Subtask", "Description", createdEpic.getId()));

        assertNotNull(createdSubtask.getId());
        assertEquals(createdSubtask, taskManager.getSubtask(createdSubtask.getId()));
        assertTrue(taskManager.getSubtasksByEpic(createdEpic.getId()).contains(createdSubtask));
    }

    @Test
    void shouldNotCreateSubtaskWithoutEpic() {
        assertNull(taskManager.createSubtask(subtask));
    }

    @Test
    void shouldGetAllTasks() {
        Task task1 = taskManager.createTask(new Task("Task1", "Desc"));
        Task task2 = taskManager.createTask(new Task("Task2", "Desc"));

        List<Task> allTasks = taskManager.getAllTasks();
        assertEquals(2, allTasks.size());
        assertTrue(allTasks.contains(task1));
        assertTrue(allTasks.contains(task2));
    }

    @Test
    void shouldUpdateTask() {
        Task createdTask = taskManager.createTask(task);
        // Используем конструктор с 2 параметрами и устанавливаем ID через сеттер
        Task updatedTask = new Task("Updated", "New desc");
        updatedTask.setId(createdTask.getId());
        updatedTask.setStatus(createdTask.getStatus()); // сохраняем статус

        assertTrue(taskManager.updateTask(updatedTask));
        assertEquals("Updated", taskManager.getTask(createdTask.getId()).getTitle());
    }

    @Test
    void shouldNotUpdateNonExistentTask() {
        Task nonExistentTask = new Task("Task", "Desc");
        nonExistentTask.setId(999);
        assertFalse(taskManager.updateTask(nonExistentTask));
    }

    @Test
    void shouldDeleteTask() {
        Task createdTask = taskManager.createTask(task);
        assertTrue(taskManager.deleteTask(createdTask.getId()));
        assertNull(taskManager.getTask(createdTask.getId()));
    }

    @Test
    void shouldNotDeleteNonExistentTask() {
        assertFalse(taskManager.deleteTask(999));
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask(new Task("Task1", "Desc"));
        taskManager.createTask(new Task("Task2", "Desc"));

        taskManager.deleteAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void shouldUpdateEpicStatus() {
        Epic createdEpic = taskManager.createEpic(epic);
        Subtask subtask1 = taskManager.createSubtask(
                new Subtask("Sub1", "Desc", createdEpic.getId()));
        Subtask subtask2 = taskManager.createSubtask(
                new Subtask("Sub2", "Desc", createdEpic.getId()));

        // All NEW
        assertEquals(TaskStatus.NEW, createdEpic.getStatus());

        // One DONE
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, createdEpic.getStatus());

        // All DONE
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        assertEquals(TaskStatus.DONE, createdEpic.getStatus());
    }

    @Test
    void shouldCalculateEpicTime() {
        Epic createdEpic = taskManager.createEpic(epic);
        LocalDateTime startTime = LocalDateTime.now();

        Subtask subtask1 = new Subtask("Sub1", "Desc", createdEpic.getId());
        subtask1.setStartTime(startTime);
        subtask1.setDuration(Duration.ofHours(1));
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Sub2", "Desc", createdEpic.getId());
        subtask2.setStartTime(startTime.plusHours(2));
        subtask2.setDuration(Duration.ofHours(2));
        taskManager.createSubtask(subtask2);

        assertEquals(startTime, createdEpic.getStartTime());
        assertEquals(Duration.ofHours(3), createdEpic.getDuration());
        assertEquals(startTime.plusHours(4), createdEpic.getEndTime());
    }

    @Test
    void shouldDetectTaskOverlap() {
        Task task1 = taskManager.createTask(new Task("Task1", "Desc"));
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(1));

        Task task2 = new Task("Task2", "Desc");
        task2.setStartTime(task1.getStartTime().plusMinutes(30));
        task2.setDuration(Duration.ofHours(1));

        assertTrue(taskManager.isTaskOverlapping(task2));
    }

    @Test
    void shouldGetPrioritizedTasks() {
        Task task1 = new Task("Task1", "Desc");
        task1.setStartTime(LocalDateTime.now().plusHours(2));
        taskManager.createTask(task1);

        Task task2 = new Task("Task2", "Desc");
        task2.setStartTime(LocalDateTime.now());
        taskManager.createTask(task2);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(task2, prioritized.get(0));
        assertEquals(task1, prioritized.get(1));
    }

    @Test
    void shouldAddToHistoryWhenGettingTask() {
        Task createdTask = taskManager.createTask(task);
        taskManager.getTask(createdTask.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(createdTask, history.get(0));
    }

    @Test
    void shouldNotAddDuplicatesToHistory() {
        Task createdTask = taskManager.createTask(task);
        taskManager.getTask(createdTask.getId());
        taskManager.getTask(createdTask.getId());

        assertEquals(1, taskManager.getHistory().size());
    }
}