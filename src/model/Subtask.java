package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private final int epicId;

    // Проверка валидности epicId
    private static final int MIN_EPIC_ID = 1;


    public Subtask(String title, String description, int epicId) {
        super(title, description);
        validateEpicId(epicId);
        this.epicId = epicId;
    }


    public Subtask(int id, String title, String description, int epicId) {
        super(id, title, description, TaskStatus.NEW);
        validateEpicId(epicId);
        this.epicId = epicId;
    }


    public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status);
        validateEpicId(epicId);
        this.epicId = epicId;
    }


    public Subtask(int id, String title, String description, TaskStatus status,
                   LocalDateTime startTime, Duration duration, int epicId) {
        super(id, title, description, status, startTime, duration);
        validateEpicId(epicId);
        this.epicId = epicId;
    }

    private void validateEpicId(int epicId) {
        if (epicId < MIN_EPIC_ID) {
            throw new IllegalArgumentException("Epic ID должен быть положительным и не менее " + MIN_EPIC_ID);
        }
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Subtask that = (Subtask) o;
        return epicId == that.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
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
