
package http;

import manager.Managers;
import manager.TaskManager;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class TaskHandlerTest {
    private HttpTaskServer server;
    private TaskManager taskManager;

    @BeforeEach
    public void setUp() throws Exception {
        taskManager = Managers.getDefault();
        server = new HttpTaskServer(taskManager);
        server.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testCreateTask_Success() throws Exception {
        String jsonBody = "{\n" +
                "  \"title\": \"Test Task\",\n" +
                "  \"description\": \"Description of Test Task\",\n" +
                "  \"status\": \"NEW\"\n" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());

        assertFalse(taskManager.getAllTasks().isEmpty());
        Task createdTaskInManager = taskManager.getAllTasks().get(0);
        assertEquals("Test Task", createdTaskInManager.getTitle());
        assertEquals("Description of Test Task", createdTaskInManager.getDescription());
        assertNotEquals(0, createdTaskInManager.getId());
    }

    @Test
    public void testGetAllTasks_Success() throws Exception {
        taskManager.createTask(new Task("Test Task 1", "Description 1"));
        taskManager.createTask(new Task("Test Task 2", "Description 2"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());
        assertTrue(response.body().contains("Test Task 1"));
        assertTrue(response.body().contains("Test Task 2"));
    }

    @Test
    public void testUpdateTask_Success() throws Exception {
        Task taskToCreate = new Task("Original Task", "Original Description");
        Task createdTask = taskManager.createTask(taskToCreate);
        assertNotNull(createdTask);
        int originalId = createdTask.getId();

        String jsonBody = "{\n" +
                "  \"id\": " + originalId + ",\n" +
                "  \"title\": \"Updated Task\",\n" +
                "  \"description\": \"Updated Description\",\n" +
                "  \"status\": \"IN_PROGRESS\"\n" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + originalId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());

        Task taskFromManager = taskManager.getTask(originalId);
        assertNotNull(taskFromManager);
        assertEquals("Updated Task", taskFromManager.getTitle());
        assertEquals("Updated Description", taskFromManager.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskFromManager.getStatus());
    }

    @Test
    public void testDeleteTask_Success() throws Exception {
        Task taskToCreate = new Task("Task to Delete", "Description");
        Task createdTask = taskManager.createTask(taskToCreate);
        assertNotNull(createdTask);
        int taskId = createdTask.getId();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getTask(taskId));
    }

    @Test
    public void testGetTask_NotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteTask_NotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}