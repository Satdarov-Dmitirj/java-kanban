package manager;

import exceptions.ManagerSaveException;
import model.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        super(Managers.getDefaultHistory());
        this.file = file;
    }

    private void save() {
        try {
            StringBuilder data = new StringBuilder("id,type,name,status,description,epic\n");

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

    private String toString(Task task) {
        String[] fields = {
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getTitle(),
                task.getStatus().name(),
                task.getDescription(),
                task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : ""
        };
        return String.join(",", fields);
    }

    private static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                return new Subtask(id, name, description, status, epicId);
            default:
                throw new IllegalStateException("Неизвестный тип задачи: " + type);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            if (lines.length <= 1) return manager;

            List<Task> tasks = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                tasks.add(fromString(lines[i]));
            }

            for (Task task : tasks) {
                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        manager.subtasks.put(task.getId(), (Subtask) task);
                        break;
                    default:
                        throw new IllegalStateException("Неизвестный тип задачи: " + task.getType());
                }
            }

            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtask(subtask.getId());
                }
            }

            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic.getId());
            }

            manager.nextId = tasks.stream()
                    .mapToInt(Task::getId)
                    .max().orElse(0) + 1;

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean result = super.updateTask(task);
        if (result) save();
        return result;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean result = super.updateEpic(epic);
        if (result) save();
        return result;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean result = super.updateSubtask(subtask);
        if (result) save();
        return result;
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
        boolean result = super.deleteTask(id);
        if (result) save();
        return result;
    }

    @Override
    public boolean deleteEpic(int id) {
        boolean result = super.deleteEpic(id);
        if (result) save();
        return result;
    }

    @Override
    public boolean deleteSubtask(int id) {
        boolean result = super.deleteSubtask(id);
        if (result) save();
        return result;
    }
}