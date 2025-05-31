package manager;

import model.*;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;
    private int nextId = 1;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) return null;
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) return null;
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null || !epics.containsKey(subtask.getEpicId())) {
            return null;
        }
        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        updateEpicStatus(epic.getId());
        return subtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getAllTasks() {
        return tasks.values().stream()
                .filter(task -> !(task instanceof Epic || task instanceof Subtask))
                .collect(Collectors.toList());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        if (!epics.containsKey(epicId)) return Collections.emptyList();
        return epics.get(epicId).getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public boolean updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return false;
        tasks.put(task.getId(), task);
        return true;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return false;
        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setTitle(epic.getTitle());
        savedEpic.setDescription(epic.getDescription());
        return true;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return false;
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
        return true;
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.keySet().removeIf(id -> !(tasks.get(id) instanceof Epic || tasks.get(id) instanceof Subtask));
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            updateEpicStatus(epic.getId());
        });
    }

    @Override
    public void deleteAllEpics() {
        subtasks.keySet().forEach(historyManager::remove);
        epics.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.clear();
    }

    @Override
    public boolean deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null && !(task instanceof Epic || task instanceof Subtask)) {
            historyManager.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) return false;

        epic.getSubtaskIds().forEach(subtaskId -> {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        });
        historyManager.remove(id);
        return true;
    }

    @Override
    public boolean deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) return false;

        historyManager.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtask(id);
            updateEpicStatus(epic.getId());
        }
        return true;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> subtasks = getSubtasksByEpic(epicId);
        if (subtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != TaskStatus.NEW) allNew = false;
            if (subtask.getStatus() != TaskStatus.DONE) allDone = false;
        }

        if (allNew) epic.setStatus(TaskStatus.NEW);
        else if (allDone) epic.setStatus(TaskStatus.DONE);
        else epic.setStatus(TaskStatus.IN_PROGRESS);
    }
}