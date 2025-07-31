
package http;

import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class EpicHandlerTest {
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
    public void testCreateEpic_Success() throws Exception {
        String jsonBody = "{\n" +
                "  \"title\": \"Test Epic\",\n" +
                "  \"description\": \"Description of Test Epic\"\n" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());

        assertFalse(taskManager.getAllEpics().isEmpty());
        Epic createdEpicInManager = taskManager.getAllEpics().get(0);
        assertEquals("Test Epic", createdEpicInManager.getTitle());
        assertEquals("Description of Test Epic", createdEpicInManager.getDescription());
        assertNotEquals(0, createdEpicInManager.getId());
    }

    @Test
    public void testGetAllEpics_Success() throws Exception {
        taskManager.createEpic(new Epic("Test Epic 1", "Description 1"));
        taskManager.createEpic(new Epic("Test Epic 2", "Description 2"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());
        assertTrue(response.body().contains("Test Epic 1"));
        assertTrue(response.body().contains("Test Epic 2"));
    }

    @Test
    public void testGetEpicById_Success() throws Exception {
        Epic epicToCreate = new Epic("Test Epic", "Description");
        Epic createdEpic = taskManager.createEpic(epicToCreate);
        assertNotNull(createdEpic);
        int epicId = createdEpic.getId();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());
        assertTrue(response.body().contains("Test Epic"));
    }

    @Test
    public void testGetEpicById_NotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateEpic_Success() throws Exception {
        Epic epicToCreate = new Epic("Original Epic", "Original Description");
        Epic createdEpic = taskManager.createEpic(epicToCreate);
        assertNotNull(createdEpic);
        int originalId = createdEpic.getId();

        String jsonBody = "{\n" +
                "  \"id\": " + originalId + ",\n" +
                "  \"title\": \"Updated Epic\",\n" +
                "  \"description\": \"Updated Description\"\n" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + originalId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());

        Epic epicFromManager = taskManager.getEpic(originalId);
        assertNotNull(epicFromManager);
        assertEquals("Updated Epic", epicFromManager.getTitle());
        assertEquals("Updated Description", epicFromManager.getDescription());
    }

    @Test
    public void testDeleteEpic_Success() throws Exception {
        Epic epicToCreate = new Epic("Epic to Delete", "Description");
        Epic createdEpic = taskManager.createEpic(epicToCreate);
        assertNotNull(createdEpic);
        int epicId = createdEpic.getId();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getEpic(epicId));
    }

    @Test
    public void testDeleteEpic_NotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetSubtasksByEpicId_Success() throws Exception {
        Epic epic = new Epic("Epic for Subtasks", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        int epicId = createdEpic.getId();

        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", epicId);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());
        assertTrue(response.body().contains("Subtask 1"));
        assertTrue(response.body().contains("Subtask 2"));
    }

    @Test
    public void testGetSubtasksByEpicId_EpicNotFound() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999999/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}
