package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import manager.Managers;
import manager.TaskManager;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PrioritizedHandlerTest {
    private HttpTaskServer server;
    private TaskManager taskManager;
    private Gson gson;

    @BeforeEach
    public void setUp() throws Exception {
        taskManager = Managers.getDefault();
        server = new HttpTaskServer(taskManager);
        server.start();
        gson = new Gson();
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testGetPrioritizedTasks_Success_Empty() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertEquals("[]", response.body().trim());
    }

    @Test
    public void testGetPrioritizedTasks_Success_WithItems() throws Exception {
        Task task1 = new Task("Task Z", "Description Z");
        Task task2 = new Task("Task A", "Description A");
        Task createdTask1 = taskManager.createTask(task1);
        Task createdTask2 = taskManager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());
        assertTrue(response.body().contains("Task Z"));
        assertTrue(response.body().contains("Task A"));

        Type listType = new TypeToken<List<Task>>(){}.getType();
        List<Task> prioritized = gson.fromJson(response.body(), listType);
        assertEquals(2, prioritized.size());
        assertEquals(createdTask1.getId(), prioritized.get(0).getId());
        assertEquals(createdTask2.getId(), prioritized.get(1).getId());
    }

    @Test
    public void testGetPrioritizedTasks_MethodNotAllowed() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }
}