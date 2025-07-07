package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
        if (id <= 0) {
            throw new IllegalArgumentException("Epic ID must be positive");
        }
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtask(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public void updateStatus(List<Subtask> allSubtasks) {
        if (subtaskIds.isEmpty()) {
            setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : allSubtasks) {
            if (subtask.getEpicId() == this.getId()) {
                if (subtask.getStatus() != TaskStatus.NEW) allNew = false;
                if (subtask.getStatus() != TaskStatus.DONE) allDone = false;
            }
        }

        if (allNew) {
            setStatus(TaskStatus.NEW);
        } else if (allDone) {
            setStatus(TaskStatus.DONE);
        } else {
            setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}