package manager;

import model.*;
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

    boolean updateTask(Task task);

    boolean updateEpic(Epic epic);

    boolean updateSubtask(Subtask subtask);

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    boolean deleteTask(int id);

    boolean deleteEpic(int id);

    boolean deleteSubtask(int id);

    List<Task> getHistory();
}
