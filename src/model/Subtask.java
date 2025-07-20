package model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        if (epicId <= 0) {
            throw new IllegalArgumentException("Epic ID must be positive");
        }
        this.epicId = epicId;
    }

    public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status);
        if (epicId <= 0) {
            throw new IllegalArgumentException("Epic ID must be positive");
        }
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
        return "Subtask{id=" + getId() +
                ", type=" + getType() +
                ", title='" + getTitle() + '\'' +
                ", status=" + getStatus() +
                ", description='" + getDescription() + '\'' +
                ", epicId=" + epicId +
                '}';
    }
}