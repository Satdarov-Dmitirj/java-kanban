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
    void epicShouldReturnCorrectType() {
        Epic epic = new Epic("Epic", "Desc");
        assertEquals(TaskType.EPIC, epic.getType());
    }

    @Test
    void epicToStringShouldContainSubtaskIds() {
        Epic epic = new Epic(1, "Epic", "Desc", TaskStatus.NEW);
        epic.addSubtask(2);
        String str = epic.toString();
        assertTrue(str.contains("subtaskIds=[2]"));
    }

    @Test
    void shouldUpdateStatusBasedOnSubtasks() {
        Epic epic = new Epic("Epic", "Desc");
        epic.updateStatus(List.of(
                new Subtask("Sub1", "Desc", epic.getId()),
                new Subtask("Sub2", "Desc", epic.getId())
        ));
        assertEquals(TaskStatus.NEW, epic.getStatus());
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
    void shouldNotAcceptNullEpicId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Subtask("Sub", "Desc", 0);
        });
    }
}