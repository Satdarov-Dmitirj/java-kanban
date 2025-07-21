package manager;

import model.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager;
    protected int nextId = 1;

    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(
                    Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder())
            )
    );

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public Task createTask(Task task) {
        if (task == null || isTaskOverlapping(task)) {
            return null;
        }
        task.setId(nextId++);
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
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
        if (subtask == null || !epics.containsKey(subtask.getEpicId()) || isTaskOverlapping(subtask)) {
            return null;
        }
        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);
        addToPrioritizedTasks(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        updateEpicStatus(epic.getId());
        updateEpicTime(epic.getId());

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
        return new ArrayList<>(tasks.values());
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
    public boolean updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId()) || isTaskOverlapping(task)) {
            return false;
        }
        removeFromPrioritizedTasks(tasks.get(task.getId()));
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
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
        if (subtask == null || !subtasks.containsKey(subtask.getId()) || isTaskOverlapping(subtask)) {
            return false;
        }
        removeFromPrioritizedTasks(subtasks.get(subtask.getId()));
        subtasks.put(subtask.getId(), subtask);
        addToPrioritizedTasks(subtask);
        updateEpicStatus(subtask.getEpicId());
        updateEpicTime(subtask.getEpicId());
        return true;
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().forEach(this::removeFromPrioritizedTasks);
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(this::removeFromPrioritizedTasks);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(this::removeFromPrioritizedTasks);
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
        });
    }

    @Override
    public boolean deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritizedTasks(task);
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
            Task subtask = subtasks.remove(subtaskId);
            if (subtask != null) {
                removeFromPrioritizedTasks(subtask);
            }
            historyManager.remove(subtaskId);
        });
        historyManager.remove(id);
        return true;
    }

    @Override
    public boolean deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) return false;

        removeFromPrioritizedTasks(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtask(id);
            updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
        }
        historyManager.remove(id);
        return true;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean isTaskOverlapping(Task task) {
        if (task.getStartTime() == null) return false;
        return prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getId() != task.getId())
                .anyMatch(existingTask -> isTasksOverlap(task, existingTask));
    }

    @Override
    public void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null || epic.getSubtaskIds().isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null && subtask.getStartTime() != null) {

                if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subtask.getStartTime();
                }


                LocalDateTime subtaskEnd = subtask.getEndTime();
                if (latestEnd == null || subtaskEnd.isAfter(latestEnd)) {
                    latestEnd = subtaskEnd;
                }


                if (subtask.getDuration() != null) {
                    totalDuration = totalDuration.plus(subtask.getDuration());
                }
            }
        }

        if (earliestStart != null && latestEnd != null) {
            epic.setStartTime(earliestStart);
            epic.setDuration(totalDuration);
            epic.setEndTime(latestEnd);
        } else {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
        }
    }

    @Override
    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> subtasks = getSubtasksByEpic(epicId);
        if (subtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = subtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.NEW);
        boolean allDone = subtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.DONE);

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public LocalDateTime getTaskEndTime(int id) {
        Task task = tasks.get(id);
        if (task != null) return task.getEndTime();

        Subtask subtask = subtasks.get(id);
        if (subtask != null) return subtask.getEndTime();

        Epic epic = epics.get(id);
        if (epic != null) return epic.getEndTime();

        return null;
    }

    protected boolean isTasksOverlap(Task task1, Task task2) {
        if (task1 == task2 || task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    protected void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    protected void removeFromPrioritizedTasks(Task task) {
        prioritizedTasks.remove(task);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}