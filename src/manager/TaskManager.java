package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManager {
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);


    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    List<Subtask> getSubtasksByEpic(int epicId);

    default boolean updateTask(Task task) {
        return false;
    }

    boolean updateEpic(Epic epic);

    boolean updateSubtask(Subtask subtask);


    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    boolean deleteTask(int id);

    boolean deleteEpic(int id);

    boolean deleteSubtask(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    boolean isTaskOverlapping(Task task);

    void updateEpicTime(int epicId);

    void updateEpicStatus(int epicId);

    LocalDateTime getTaskEndTime(int id);
}