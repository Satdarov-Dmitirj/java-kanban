package model;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void subtaskTypeShouldBeSubtask() {
        Subtask subtask = new Subtask("Sub", "Desc", 1);
        assertEquals(TaskType.SUBTASK, subtask.getType());
    }

    @Test
    void shouldContainEpicIdInToString() {
        Subtask subtask = new Subtask(1, "Sub", "Desc", TaskStatus.NEW, 2);
        assertTrue(subtask.toString().contains("epicId=2"));
    }

    @Test
    void shouldRejectInvalidEpicId() {
        assertThrows(IllegalArgumentException.class, () -> new Subtask("Sub", "Desc", 0));
        assertThrows(IllegalArgumentException.class, () -> new Subtask(1, "Sub", "Desc", TaskStatus.NEW, -1));
    }

    @Test
    void shouldCreateWithTimeParameters() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(45);
        Subtask subtask = new Subtask(1, "Sub", "Desc", TaskStatus.NEW, start, duration, 2);

        assertEquals(start, subtask.getStartTime());
        assertEquals(duration, subtask.getDuration());
        assertEquals(start.plus(duration), subtask.getEndTime());
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask sub1 = new Subtask(1, "Sub 1", "Desc", TaskStatus.NEW, 2);
        Subtask sub2 = new Subtask(1, "Sub 2", "Different", TaskStatus.DONE, 2);

        assertEquals(sub1, sub2, "Подзадачи с одинаковым ID должны быть равны");
        assertEquals(sub1.hashCode(), sub2.hashCode(), "Хэш-коды должны совпадать для одинаковых ID");

        // Проверяем, что действительно разные поля
        assertNotEquals(sub1.getTitle(), sub2.getTitle());
        assertNotEquals(sub1.getDescription(), sub2.getDescription());
        assertNotEquals(sub1.getStatus(), sub2.getStatus());
    }

    @Test
    void shouldReturnCorrectEpicId() {
        Subtask subtask = new Subtask(1, "Sub", "Desc", 10);
        assertEquals(10, subtask.getEpicId());
    }
}