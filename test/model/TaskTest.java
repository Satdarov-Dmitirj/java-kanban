package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void taskEqualityById() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(1, "Task 2", "Different", TaskStatus.DONE);
        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void taskWithDifferentIdsNotEqual() {
        Task task1 = new Task(1, "Task", "Desc", TaskStatus.NEW);
        Task task2 = new Task(2, "Task", "Desc", TaskStatus.NEW);
        assertNotEquals(task1, task2);
    }

    @Test
    void taskTypeShouldBeTask() {
        Task task = new Task("Task", "Description");
        assertEquals(TaskType.TASK, task.getType());
    }

    @Test
    void toStringContainsAllFields() {
        LocalDateTime time = LocalDateTime.now();
        Task task = new Task(1, "Task", "Desc", TaskStatus.IN_PROGRESS, time, Duration.ofMinutes(30));
        String str = task.toString();

        assertAll(
                () -> assertTrue(str.contains("id=1")),
                () -> assertTrue(str.contains("type=TASK")),
                () -> assertTrue(str.contains("title='Task'")),
                () -> assertTrue(str.contains("status=IN_PROGRESS")),
                () -> assertTrue(str.contains("startTime=" + time)),
                () -> assertTrue(str.contains("duration=PT30M"))
        );
    }

    @Test
    void endTimeCalculation() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 10, 0);
        Duration duration = Duration.ofHours(2);
        Task task = new Task(1, "Task", "Desc", TaskStatus.NEW, start, duration);

        assertEquals(start.plus(duration), task.getEndTime());
    }

    @Test
    void endTimeShouldBeNullWhenNoStartTime() {
        Task task = new Task(1, "Task", "Desc", TaskStatus.NEW);
        assertNull(task.getEndTime());
    }
}




class EpicTest {
    private Epic epic;
    private Subtask sub1;
    private Subtask sub2;

    @BeforeEach
    void setUp() {
        epic = new Epic(1, "Epic", "Description");
        sub1 = new Subtask(2, "Sub 1", "Desc", TaskStatus.NEW, 1);
        sub2 = new Subtask(3, "Sub 2", "Desc", TaskStatus.NEW, 1);
    }

    @Test
    void epicStatusNewWhenNoSubtasks() {
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicStatusNewWhenAllSubtasksNew() {
        epic.updateStatus(List.of(sub1, sub2));
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicStatusDoneWhenAllSubtasksDone() {
        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.DONE);
        epic.updateStatus(List.of(sub1, sub2));
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void epicStatusInProgressWhenMixedStatuses() {
        sub1.setStatus(TaskStatus.NEW);
        sub2.setStatus(TaskStatus.DONE);
        epic.updateStatus(List.of(sub1, sub2));
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void epicStatusInProgressWhenAllInProgress() {
        sub1.setStatus(TaskStatus.IN_PROGRESS);
        sub2.setStatus(TaskStatus.IN_PROGRESS);
        epic.updateStatus(List.of(sub1, sub2));
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldCalculateEndTimeFromSubtasks() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 10, 0);
        Duration duration = Duration.ofHours(1);

        sub1 = new Subtask(2, "Sub1", "Desc", TaskStatus.NEW, start, duration, 1);
        sub2 = new Subtask(3, "Sub2", "Desc", TaskStatus.NEW, start.plusHours(2), duration, 1);

        epic.setEndTime(sub2.getEndTime());
        assertEquals(start.plusHours(3), epic.getEndTime());
    }

    @Test
    void epicTypeShouldBeEpic() {
        assertEquals(TaskType.EPIC, epic.getType());
    }

    @Test
    void shouldManageSubtaskIds() {
        epic.addSubtask(10);
        epic.addSubtask(20);
        assertEquals(List.of(10, 20), epic.getSubtaskIds());

        epic.removeSubtask(10);
        assertEquals(List.of(20), epic.getSubtaskIds());

        epic.clearSubtasks();
        assertTrue(epic.getSubtaskIds().isEmpty());
    }
}


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
        assertThrows(IllegalArgumentException.class, () -> new Subtask(1, "Sub", "Desc", -1));
    }

    @Test
    void shouldCreateWithTimeParameters() {
        LocalDateTime start = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(45);
        Subtask subtask = new Subtask(1, "Sub", "Desc", TaskStatus.NEW, start, duration, 2);

        assertAll(
                () -> assertEquals(start, subtask.getStartTime()),
                () -> assertEquals(duration, subtask.getDuration()),
                () -> assertEquals(start.plus(duration), subtask.getEndTime())
        );
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask sub1 = new Subtask(1, "Sub 1", "Desc", TaskStatus.NEW, 2);
        Subtask sub2 = new Subtask(1, "Sub 2", "Different", TaskStatus.DONE, 2);
        assertEquals(sub1, sub2);
    }

    @Test
    void shouldReturnCorrectEpicId() {
        Subtask subtask = new Subtask(1, "Sub", "Desc", 10);
        assertEquals(10, subtask.getEpicId());
    }
}