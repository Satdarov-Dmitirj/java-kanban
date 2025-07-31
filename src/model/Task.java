package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;
    private Duration duration;
    private LocalDateTime startTime;
    private TaskType type;

    private static final int MIN_ID = 1;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.type = TaskType.TASK;
        this.duration = Duration.ZERO; // Инициализация по умолчанию
    }

    public Task(int id, String title, String description, TaskStatus status) {
        this(title, description);
        this.id = id;
        this.status = status;
        validateId(id);
    }

    public Task(int id, String title, String description, TaskStatus status,
                LocalDateTime startTime, Duration duration) {
        this(id, title, description, status);
        this.startTime = startTime;
        this.duration = duration != null ? duration : Duration.ZERO;
    }

    private void validateId(int id) {
        if (id < MIN_ID) {
            throw new IllegalArgumentException("ID должен быть положительным и не менее " + MIN_ID);
        }
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        validateId(id);
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration != null ? duration : Duration.ZERO;
    }

    public void setDuration(Duration duration) {
        this.duration = duration != null ? duration : Duration.ZERO;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public TaskType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id &&
                Objects.equals(title, task.title) &&
                Objects.equals(description, task.description) &&
                status == task.status &&
                Objects.equals(getDuration(), task.getDuration()) &&
                Objects.equals(startTime, task.startTime) &&
                type == task.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status, getDuration(), startTime, type);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", duration=" + getDuration() +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }
}