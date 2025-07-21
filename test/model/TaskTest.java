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
        String str = subtask.toString();
        assertTrue(str.contains("epicId=2"), "toString() должен содержать epicId");
    }

    @Test
    void shouldRejectInvalidEpicId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Subtask("Sub", "Desc", 0),
                "Должно выбрасываться исключение при epicId = 0");

        assertThrows(IllegalArgumentException.class,
                () -> new Subtask(1, "Sub", "Desc", TaskStatus.NEW, -1),
                "Должно выбрасываться исключение при epicId = -1");
    }

    @Test
    void shouldCreateWithTimeParameters() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 10, 0);
        Duration duration = Duration.ofMinutes(45);
        Subtask subtask = new Subtask(1, "Sub", "Desc", TaskStatus.NEW, start, duration, 2);

        assertEquals(start, subtask.getStartTime(), "Некорректное время начала");
        assertEquals(duration, subtask.getDuration(), "Некорректная продолжительность");
        assertEquals(start.plus(duration), subtask.getEndTime(), "Некорректное время окончания");
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask sub1 = new Subtask(1, "Sub 1", "Desc", TaskStatus.NEW, 2);
        Subtask sub2 = new Subtask(1, "Sub 2", "Different", TaskStatus.DONE, 2);

        assertEquals(sub1, sub2, "Подзадачи с одинаковым ID должны быть равны");
        assertEquals(sub1.hashCode(), sub2.hashCode(), "Хэш-коды подзадач с одинаковым ID должны совпадать");
    }

    @Test
    void shouldReturnCorrectEpicId() {
        Subtask subtask = new Subtask(1, "Sub", "Desc", 10);
        assertEquals(10, subtask.getEpicId(), "Некорректный epicId");
    }
}