package model;

import org.junit.jupiter.api.Test;
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
    void taskShouldNotBeEqualWhenIdsDifferent() {
        Task task1 = new Task("Same Name", "Same Desc");
        task1.setId(1);
        Task task2 = new Task("Same Name", "Same Desc");
        task2.setId(2);

        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны");
    }

    @Test
    void shouldChangeStatusCorrectly() {
        Task task = new Task("Task", "Desc");
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }
}

class EpicTest {
    @Test
    void newEpicShouldHaveNewStatus() {
        Epic epic = new Epic("Epic", "Description");
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicShouldNotAddSelfAsSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(1);
        assertThrows(IllegalArgumentException.class, () -> {
            Subtask subtask = new Subtask("Invalid", "Desc", epic.getId());
            subtask.setId(epic.getId()); // Попытка создать подзадачу с ID эпика
        });
    }
}

class SubtaskTest {
    @Test
    void subtaskShouldReferenceEpic() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(1);
        Subtask subtask = new Subtask("Subtask", "Desc", epic.getId());

        assertEquals(epic.getId(), subtask.getEpicId());
    }

    @Test
    void subtaskShouldNotReferenceNonexistentEpic() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Subtask("Subtask", "Desc", 999); // Несуществующий эпик
        });
    }
}