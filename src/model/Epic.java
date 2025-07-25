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
        if (allSubtasks == null || allSubtasks.isEmpty()) {
            setStatus(TaskStatus.NEW);
            return;
        }

        boolean hasNew = false;
        boolean hasInProgress = false;
        boolean hasDone = false;

        for (Subtask subtask : allSubtasks) {
            if (subtask.getEpicId() == this.getId()) {
                switch (subtask.getStatus()) {
                    case NEW:
                        hasNew = true;
                        break;
                    case IN_PROGRESS:
                        hasInProgress = true;
                        break;
                    case DONE:
                        hasDone = true;
                        break;
                    default:
                        throw new IllegalStateException("Неизвестный статус подзадачи: " + subtask.getStatus());
                }
            }
        }

        if (hasInProgress || (hasNew && hasDone)) {
            setStatus(TaskStatus.IN_PROGRESS);
        } else if (hasDone && !hasNew && !hasInProgress) {
            setStatus(TaskStatus.DONE);
        } else {
            setStatus(TaskStatus.NEW);
        }
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "Epic{id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", status=" + getStatus() +
                ", description='" + getDescription() + '\'' +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}