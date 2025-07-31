package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
        this.setStatus(TaskStatus.NEW);
    }

    public Epic(int id, String title, String description) {
        super(id, title, description, TaskStatus.NEW);
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
        this.setStartTime(null);
        this.setDuration(Duration.ZERO);
    }

    public void updateTimeParameters(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            this.setStartTime(null);
            this.setDuration(Duration.ZERO);
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        boolean hasValidTimeData = false;

        for (Subtask subtask : subtasks) {
            if (subtask != null && subtask.getStartTime() != null && subtask.getDuration() != null) {
                hasValidTimeData = true;
                LocalDateTime start = subtask.getStartTime();
                LocalDateTime end = start.plus(subtask.getDuration());

                if (earliestStart == null || start.isBefore(earliestStart)) {
                    earliestStart = start;
                }

                if (latestEnd == null || end.isAfter(latestEnd)) {
                    latestEnd = end;
                }
            }
        }

        if (hasValidTimeData) {
            this.setStartTime(earliestStart);
            this.setDuration(Duration.between(earliestStart, latestEnd));
        } else {
            this.setStartTime(null);
            this.setDuration(Duration.ZERO);
        }
    }

    public void updateStatus(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasks) {
            if (subtask == null) continue;

            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
            if (!allNew && !allDone) {
                break;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", startTime=" + getStartTime() +
                ", duration=" + getDuration() +
                ", endTime=" + getEndTime() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}