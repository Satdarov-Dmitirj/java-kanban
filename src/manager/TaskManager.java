package manager;

import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;

    public Task createTask(Task task) {
        if (task == null) return null;

        task.setId(nextId++);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        if (epic == null) return null;

        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        tasks.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null || !epics.containsKey(subtask.getEpicId())) {
            return null;
        }

        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);
        tasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        epic.updateStatus(getAllSubtasks());

        return subtask;
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public List<Task> getAllTasks() {
        return tasks.values().stream()
                .filter(task -> !(task instanceof Epic || task instanceof Subtask))
                .collect(Collectors.toList());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getSubtasksByEpic(int epicId) {
        if (!epics.containsKey(epicId)) return Collections.emptyList();

        return epics.get(epicId).getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return false;

        tasks.put(task.getId(), task);
        return true;
    }

    public boolean updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return false;

        epics.put(epic.getId(), epic);
        tasks.put(epic.getId(), epic);
        return true;
    }

    public boolean updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return false;

        subtasks.put(subtask.getId(), subtask);
        tasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.updateStatus(getAllSubtasks());
        }
        return true;
    }

    public void deleteAllTasks() {
        List<Integer> toRemove = tasks.keySet().stream()
                .filter(id -> !(tasks.get(id) instanceof Epic || tasks.get(id) instanceof Subtask))
                .collect(Collectors.toList());

        toRemove.forEach(tasks::remove);
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            epic.setStatus(TaskStatus.NEW);
        });
    }

    public void deleteAllEpics() {
        subtasks.clear();
        epics.keySet().forEach(tasks::remove);
        epics.clear();
    }

    public boolean deleteTask(int id) {
        Task task = tasks.get(id);
        if (task == null || task instanceof Epic || task instanceof Subtask) {
            return false;
        }
        return tasks.remove(id) != null;
    }

    public boolean deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) return false;

        tasks.remove(id);
        epic.getSubtaskIds().forEach(subtaskId -> {
            subtasks.remove(subtaskId);
            tasks.remove(subtaskId);
        });

        return true;
    }

    public boolean deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) return false;

        tasks.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtask(id);
            epic.updateStatus(getAllSubtasks());
        }

        return true;
    }
}