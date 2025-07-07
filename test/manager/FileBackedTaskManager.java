package manager;

import exceptions.ManagerSaveException;
import model.*;
import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
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
        Task task = manager.createTask(new Task("Task", "Desc"));
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = manager.createSubtask(new Subtask("Sub", "Desc", epic.getId()));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getAllTasks().size());
        assertEquals(1, loaded.getAllEpics().size());
        assertEquals(1, loaded.getAllSubtasks().size());
        assertEquals(task.getTitle(), loaded.getTask(task.getId()).getTitle());
        assertEquals(subtask.getEpicId(), loaded.getSubtask(subtask.getId()).getEpicId());
    }

    @Test
    void shouldThrowExceptionWhenFileInvalid() {
        File invalid = new File("/invalid/path/tasks.csv");
        assertThrows(ManagerSaveException.class, () -> {
            new FileBackedTaskManager(invalid).createTask(new Task("Task", "Desc"));
        });
    }
}