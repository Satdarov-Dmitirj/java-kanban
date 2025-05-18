package manager;

import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
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

    public void updateStatus(List<Subtask> allSubtasks) {
        if (subtaskIds.isEmpty()) {
            setStatus(TaskStatus.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (int subtaskId : subtaskIds) {
            for (Subtask subtask : allSubtasks) {
                if (subtask.getId() == subtaskId) {
                    if (subtask.getStatus() != TaskStatus.DONE) {
                        allDone = false;
                    }
                    if (subtask.getStatus() != TaskStatus.NEW) {
                        allNew = false;
                    }
                    break;
                }
            }
            if (!allDone && !allNew) break;
        }

        if (allDone) {
            setStatus(TaskStatus.DONE);
        } else if (allNew) {
            setStatus(TaskStatus.NEW);
        } else {
            setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}