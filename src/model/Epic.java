package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;
    private Duration duration = Duration.ZERO;
    private LocalDateTime startTime;

    public Epic(String title, String description) {
        super(title, description);
    }

    public Epic(int id, String title, String description) {
        super(id, title, description, TaskStatus.NEW);
    }

    public Epic(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
    }

    public Epic(int id, String title, String description, TaskStatus status,
                LocalDateTime startTime, Duration duration) {
        super(id, title, description, status, startTime, duration);
        this.duration = duration;
        this.startTime = startTime;
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
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.endTime = null;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void updateTimeParameters(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            this.startTime = null;
            this.duration = Duration.ZERO;
            this.endTime = null;
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (Subtask subtask : subtasks) {
            if (subtask.getStartTime() != null) {
                if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subtask.getStartTime();
                }

                LocalDateTime subtaskEnd = subtask.getEndTime();
                if (latestEnd == null || subtaskEnd.isAfter(latestEnd)) {
                    latestEnd = subtaskEnd;
                }
            }

            if (subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
        }

        this.startTime = earliestStart;
        this.endTime = latestEnd;
        this.duration = totalDuration;
    }

    public void updateStatus(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
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
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds) &&
                Objects.equals(endTime, epic.endTime) &&
                Objects.equals(duration, epic.duration) &&
                Objects.equals(startTime, epic.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds, endTime, duration, startTime);
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