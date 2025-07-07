package model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void taskShouldBeEqualWhenIdsAreEqual() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Different description");
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void taskShouldReturnCorrectType() {
        Task task = new Task("Task", "Desc");
        assertEquals(TaskType.TASK, task.getType());
    }

    @Test
    void taskToStringShouldContainAllFields() {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW);
        String str = task.toString();
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("type=TASK"));
        assertTrue(str.contains("title='Task'"));
        assertTrue(str.contains("status=NEW"));
    }
}


class EpicTest {
    @Test
    void shouldUpdateStatusBasedOnSubtasks() {
        Epic epic = new Epic(1, "Epic", "Description", TaskStatus.NEW);
        Subtask subtask1 = new Subtask(2, "Sub1", "Desc", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(3, "Sub2", "Desc", TaskStatus.NEW, epic.getId());

        epic.updateStatus(List.of(subtask1, subtask2));
        assertEquals(TaskStatus.NEW, epic.getStatus(), "При всех NEW подзадачах эпик должен быть NEW");

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        epic.updateStatus(List.of(subtask1, subtask2));
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                "При хотя бы одной IN_PROGRESS подзадаче эпик должен быть IN_PROGRESS");

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        epic.updateStatus(List.of(subtask1, subtask2));
        assertEquals(TaskStatus.DONE, epic.getStatus(), "При всех DONE подзадачах эпик должен быть DONE");

        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.DONE);
        epic.updateStatus(List.of(subtask1, subtask2));
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                "При разных статусах подзадач эпик должен быть IN_PROGRESS");
    }
}

class SubtaskTest {
    @Test
    void subtaskShouldReturnCorrectType() {
        Subtask subtask = new Subtask("Sub", "Desc", 1);
        assertEquals(TaskType.SUBTASK, subtask.getType());
    }

    @Test
    void subtaskToStringShouldContainEpicId() {
        Subtask subtask = new Subtask(1, "Sub", "Desc", TaskStatus.NEW, 2);
        String str = subtask.toString();
        assertTrue(str.contains("epicId=2"));
    }

    @Test
    void shouldNotAcceptInvalidEpicId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Subtask("Sub", "Desc", 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Subtask(1, "Sub", "Desc", TaskStatus.NEW, -1);
        });
    }
}