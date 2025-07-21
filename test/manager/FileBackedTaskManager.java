package manager;

import exceptions.ManagerSaveException;
import model.*;
import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void shouldSaveAndLoadEmptyManager() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task = manager.createTask(new Task("Task", "Description"));
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getAllTasks().size());
        assertEquals(1, loaded.getAllEpics().size());
        assertEquals(1, loaded.getAllSubtasks().size());

        Task loadedTask = loaded.getTask(task.getId());
        assertEquals(task.getTitle(), loadedTask.getTitle());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());

        Subtask loadedSubtask = loaded.getSubtask(subtask.getId());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());
    }

    @Test
    void shouldSaveAndLoadTasksWithTime() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);

        Task task = new Task("Task", "Description");
        task.setStartTime(startTime);
        task.setDuration(duration);
        task = manager.createTask(task);

        Epic epic = manager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        subtask.setStartTime(startTime.plusHours(1));
        subtask.setDuration(duration);
        subtask = manager.createSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        Task loadedTask = loaded.getTask(task.getId());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());

        Subtask loadedSubtask = loaded.getSubtask(subtask.getId());
        assertEquals(subtask.getStartTime(), loadedSubtask.getStartTime());
        assertEquals(subtask.getDuration(), loadedSubtask.getDuration());

        Epic loadedEpic = loaded.getEpic(epic.getId());
        assertEquals(subtask.getStartTime(), loadedEpic.getStartTime());
        assertEquals(subtask.getEndTime(), loadedEpic.getEndTime());
    }

    @Test
    void shouldSaveAndLoadHistory() {
        Task task = manager.createTask(new Task("Task", "Description"));
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId()));

        // Явно добавляем в историю
        manager.addToHistory(task);
        manager.addToHistory(epic);
        manager.addToHistory(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(3, loaded.getHistory().size());
        assertEquals(task.getId(), loaded.getHistory().get(0).getId());
        assertEquals(epic.getId(), loaded.getHistory().get(1).getId());
        assertEquals(subtask.getId(), loaded.getHistory().get(2).getId());
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        Files.writeString(tempFile.toPath(), "");
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getAllTasks().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenFileInvalid() {
        File invalid = new File("/invalid/path/tasks.csv");
        assertThrows(ManagerSaveException.class, () -> {
            new FileBackedTaskManager(invalid).createTask(new Task("Task", "Description"));
        });
    }

    @Test
    void shouldUpdateEpicStatusWhenLoading() {
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Sub1", "Desc", epic.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("Sub2", "Desc", epic.getId()));

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loaded.getEpic(epic.getId());

        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.getStatus());
    }

    @Test
    void shouldUpdateEpicTimeWhenLoading() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(30);

        Epic epic = manager.createEpic(new Epic("Epic", "Description"));

        Subtask subtask1 = new Subtask("Sub1", "Desc", epic.getId());
        subtask1.setStartTime(startTime);
        subtask1.setDuration(duration);
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Sub2", "Desc", epic.getId());
        subtask2.setStartTime(startTime.plusHours(1));
        subtask2.setDuration(duration);
        manager.createSubtask(subtask2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loaded.getEpic(epic.getId());

        assertEquals(startTime, loadedEpic.getStartTime());
        assertEquals(startTime.plusHours(1).plus(duration), loadedEpic.getEndTime());
        assertEquals(Duration.ofHours(1).plus(duration.multipliedBy(2)), loadedEpic.getDuration());
    }
}