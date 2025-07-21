package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    // Конструктор для создания новой подзадачи (без ID)
    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    // Конструктор для создания подзадачи с ID, но без статуса
    public Subtask(int id, String title, String description, int epicId) {
        super(id, title, description, TaskStatus.NEW);
        this.epicId = epicId;
    }

    // Конструктор для создания подзадачи с ID и статусом
    public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    // Полный конструктор с временными параметрами
    public Subtask(int id, String title, String description, TaskStatus status,
                   LocalDateTime startTime, Duration duration, int epicId) {
        super(id, title, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", startTime=" + getStartTime() +
                ", duration=" + getDuration() +
                ", endTime=" + getEndTime() +
                ", epicId=" + epicId +
                '}';
    }
}