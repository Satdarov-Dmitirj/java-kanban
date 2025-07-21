package manager;

import exceptions.ManagerSaveException;
import model.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String CSV_HEADER = "id,type,name,status,description,startTime,duration,epic\n";

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
    }

    /**
     * Загружает менеджер из файла
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath());
            if (content.isEmpty()) {
                return manager;
            }

            String[] lines = content.split("\n");
            if (lines.length <= 1) return manager;

            List<Integer> historyIds = new ArrayList<>();
            Map<Integer, Task> allTasks = new HashMap<>();

            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isEmpty()) continue;

                if (lines[i].startsWith("history,")) {
                    String[] ids = lines[i].substring(8).split(",");
                    for (String id : ids) {
                        if (!id.isEmpty()) {
                            historyIds.add(Integer.parseInt(id));
                        }
                    }
                    continue;
                }

                Task task = fromString(lines[i]);
                if (task == null) continue;

                allTasks.put(task.getId(), task);

                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        manager.addToPrioritizedTasks(task);
                        break;
                    case EPIC:
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        manager.addToPrioritizedTasks(subtask);
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtask(subtask.getId());
                        }
                        break;
                }

                if (task.getId() >= manager.nextId) {
                    manager.nextId = task.getId() + 1;
                }
            }

            // Восстановление истории просмотров
            for (int id : historyIds) {
                Task task = allTasks.get(id);
                if (task != null) {
                    manager.historyManager.add(task);
                }
            }

            // Обновление статусов и времён эпиков
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
                manager.updateEpicTime(epic.getId());
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    /**
     * Сохраняет текущее состояние менеджера в файл
     */
    private void save() {
        try {
            StringBuilder data = new StringBuilder(CSV_HEADER);

            // Сохранение истории
            List<Task> history = getHistory();
            if (!history.isEmpty()) {
                data.append("history,");
                data.append(String.join(",", history.stream()
                        .map(task -> String.valueOf(task.getId()))
                        .toArray(String[]::new)));
                data.append("\n");
            }

            // Сохранение всех задач
            for (Task task : getAllTasks()) {
                data.append(toString(task)).append("\n");
            }
            for (Epic epic : getAllEpics()) {
                data.append(toString(epic)).append("\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                data.append(toString(subtask)).append("\n");
            }

            Files.writeString(file.toPath(), data.toString());

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    // === Переопределённые методы получения задач (с автосохранением) ===

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id); // автоматически добавляет в историю
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id); // добавляет в историю
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id); // добавляет в историю
        save();
        return subtask;
    }

    // === Переопределённые методы изменений (уже с save) ===

    @Override
    public Task createTask(Task task) {
        Task created = super.createTask(task);
        if (created != null) save();
        return created;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic created = super.createEpic(epic);
        save();
        return created;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask created = super.createSubtask(subtask);
        if (created != null) save();
        return created;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean updated = super.updateTask(task);
        if (updated) save();
        return updated;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean updated = super.updateEpic(epic);
        if (updated) save();
        return updated;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean updated = super.updateSubtask(subtask);
        if (updated) save();
        return updated;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public boolean deleteTask(int id) {
        boolean deleted = super.deleteTask(id);
        if (deleted) save();
        return deleted;
    }

    @Override
    public boolean deleteEpic(int id) {
        boolean deleted = super.deleteEpic(id);
        if (deleted) save();
        return deleted;
    }

    @Override
    public boolean deleteSubtask(int id) {
        boolean deleted = super.deleteSubtask(id);
        if (deleted) save();
        return deleted;
    }

    // === Вспомогательные методы для сериализации ===

    private String toString(Task task) {
        String startTime = task.getStartTime() != null ? task.getStartTime().format(DATE_TIME_FORMATTER) : "";
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";

        return String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                startTime,
                duration,
                epicId
        );
    }

    private static Task fromString(String value) {
        try {
            String[] fields = value.split(",");
            if (fields.length < 5) return null;

            int id = Integer.parseInt(fields[0]);
            TaskType type = TaskType.valueOf(fields[1]);
            String title = fields[2];
            TaskStatus status = TaskStatus.valueOf(fields[3]);
            String description = fields[4];

            LocalDateTime startTime = fields.length > 5 && !fields[5].isEmpty()
                    ? LocalDateTime.parse(fields[5], DATE_TIME_FORMATTER)
                    : null;

            Duration duration = fields.length > 6 && !fields[6].isEmpty()
                    ? Duration.ofMinutes(Long.parseLong(fields[6]))
                    : null;

            Task task;
            switch (type) {
                case TASK:
                    task = new Task(title, description);
                    task.setId(id);
                    task.setStatus(status);
                    task.setStartTime(startTime);
                    task.setDuration(duration);
                    return task;

                case EPIC:
                    task = new Epic(title, description);
                    task.setId(id);
                    task.setStatus(status);
                    task.setStartTime(startTime);
                    task.setDuration(duration);
                    return task;

                case SUBTASK:
                    if (fields.length <= 7 || fields[7].isEmpty()) {
                        throw new IllegalArgumentException("Subtask must have epicId");
                    }
                    int epicId = Integer.parseInt(fields[7]);
                    Subtask subtask = new Subtask(title, description, epicId);
                    subtask.setId(id);
                    subtask.setStatus(status);
                    subtask.setStartTime(startTime);
                    subtask.setDuration(duration);
                    return subtask;

                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
