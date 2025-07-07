package util;

import model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CSVFormatterTest {
    @Test
    void shouldFormatTaskToString() {
        Task task = new Task(1, "Task", "Description", TaskStatus.NEW);
        String result = CSVFormatter.toString(task);
        assertEquals("1,TASK,Task,NEW,Description,", result);
    }

    @Test
    void shouldParseStringToTask() {
        String data = "1,TASK,Task,NEW,Description,";
        Task task = CSVFormatter.fromString(data);
        assertEquals(1, task.getId());
        assertEquals("Task", task.getTitle());
        assertEquals(TaskType.TASK, task.getType());
    }

    @Test
    void shouldFormatSubtaskWithEpicId() {
        Subtask subtask = new Subtask(2, "Sub", "Desc", TaskStatus.DONE, 1);
        String result = CSVFormatter.toString(subtask);
        assertEquals("2,SUBTASK,Sub,DONE,Desc,1", result);
    }

    @Test
    void shouldThrowWhenParsingInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> {
            CSVFormatter.fromString("invalid,data,string");
        });
    }
}