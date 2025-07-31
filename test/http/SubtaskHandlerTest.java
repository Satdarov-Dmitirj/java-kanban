package http;

import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskHandlerTest {

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
    public void testCreateSubtask_Success() throws Exception {
        Epic epic = new Epic("Epic for New Subtask", "Description of Epic for New Subtask");
        Epic createdEpic = taskManager.createEpic(epic);
        assertNotNull(createdEpic);
        int epicId = createdEpic.getId();

        String jsonBody = String.format("""
            {
              "title": "Newly Created Subtask",
              "description": "Description of the newly created subtask",
              "status": "NEW",
              "epicId": %d
            }
            """, epicId).trim();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());

        assertFalse(taskManager.getAllSubtasks().isEmpty());
        Subtask createdSubtaskInManager = taskManager.getAllSubtasks().get(0);
        assertEquals("Newly Created Subtask", createdSubtaskInManager.getTitle());
        assertEquals("Description of the newly created subtask", createdSubtaskInManager.getDescription());
        assertEquals(epicId, createdSubtaskInManager.getEpicId());
        assertNotEquals(0, createdSubtaskInManager.getId());
    }

    @Test
    public void testGetAllSubtasks_Success() throws Exception {
        Epic epic = new Epic("Epic for Subtasks List", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        int epicId = createdEpic.getId();

        taskManager.createSubtask(new Subtask("Test Subtask 1", "Desc 1", epicId));
        taskManager.createSubtask(new Subtask("Test Subtask 2", "Desc 2", epicId));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());
        assertTrue(response.body().contains("Test Subtask 1"));
        assertTrue(response.body().contains("Test Subtask 2"));
    }

    @Test
    public void testGetSubtaskById_Success() throws Exception {
        Epic epic = new Epic("Epic for Get By ID", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        int epicId = createdEpic.getId();

        Subtask subtaskToCreate = new Subtask("Test Subtask", "Test Description", epicId);
        Subtask createdSubtask = taskManager.createSubtask(subtaskToCreate);
        assertNotNull(createdSubtask);
        int subtaskId = createdSubtask.getId();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());
        assertTrue(response.body().contains("Test Subtask"));
    }

    @Test
    public void testGetSubtaskById_NotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateSubtask_Success() throws Exception {
        Epic epic = new Epic("Epic for Update Subtask", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        int epicId = createdEpic.getId();

        Subtask subtaskToCreate = new Subtask("Original Subtask", "Original Description", epicId);
        Subtask createdSubtask = taskManager.createSubtask(subtaskToCreate);
        assertNotNull(createdSubtask);
        int originalId = createdSubtask.getId();

        String jsonBody = String.format("""
            {
              "id": %d,
              "title": "Updated Subtask Title",
              "description": "This is the updated description.",
              "status": "IN_PROGRESS",
              "epicId": %d
            }
            """, originalId, epicId).trim();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + originalId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());

        Subtask subtaskFromManager = taskManager.getSubtask(originalId);
        assertNotNull(subtaskFromManager);
        assertEquals("Updated Subtask Title", subtaskFromManager.getTitle());
        assertEquals("This is the updated description.", subtaskFromManager.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, subtaskFromManager.getStatus());
    }

    @Test
    public void testDeleteSubtask_Success() throws Exception {
        Epic epic = new Epic("Epic for Delete Subtask", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        int epicId = createdEpic.getId();

        Subtask subtaskToCreate = new Subtask("Subtask to Delete", "Description", epicId);
        Subtask createdSubtask = taskManager.createSubtask(subtaskToCreate);
        assertNotNull(createdSubtask);
        int subtaskId = createdSubtask.getId();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getSubtask(subtaskId));
    }

    @Test
    public void testDeleteSubtask_NotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetSubtasksByEpicId_Success() throws Exception {
        Epic epic = new Epic("Test Epic for Subtasks", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        int epicId = createdEpic.getId();

        Subtask subtask1 = new Subtask("Subtask 1 for Epic", "Desc 1", epicId);
        Subtask subtask2 = new Subtask("Subtask 2 for Epic", "Desc 2", epicId);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/epic/" + epicId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());
        assertTrue(response.body().contains("Subtask 1 for Epic"));
        assertTrue(response.body().contains("Subtask 2 for Epic"));
    }

    @Test
    public void testGetSubtasksByEpicId_EpicNotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/epic/999999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}