package http;

import com.google.gson.Gson;
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
    public void testCreateSubtask_Success() throws Exception {

        Epic epic = new Epic("Test Epic", "Description of Test Epic");
        Epic createdEpic = taskManager.createEpic(epic);
        assertNotNull(createdEpic, "Эпик должен быть успешно создан");

        Subtask subtask = new Subtask("Test Subtask", "Description of Test Subtask", createdEpic.getId());
        String jsonBody = gson.toJson(subtask);


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(201, response.statusCode(), "Ожидается код 201 при успешном создании подзадачи");
        assertNotNull(response.body(), "Тело ответа не должно быть null");
        assertFalse(response.body().isEmpty(), "Тело ответа не должно быть пустым");


        assertFalse(taskManager.getAllSubtasks().isEmpty(), "Список подзадач в менеджере не должен быть пустым после создания");
        Subtask createdSubtaskInManager = taskManager.getAllSubtasks().get(0);
        assertEquals("Test Subtask", createdSubtaskInManager.getTitle(), "Название подзадачи должно совпадать");
        assertEquals("Description of Test Subtask", createdSubtaskInManager.getDescription(), "Описание подзадачи должно совпадать");
        assertEquals(createdEpic.getId(), createdSubtaskInManager.getEpicId(), "ID эпика должен совпадать");
        assertNotEquals(0, createdSubtaskInManager.getId(), "ID подзадачи должен быть назначен");
    }

    @Test
    public void testGetAllSubtasks_Success() throws Exception {

        Epic epic = new Epic("Test Epic", "Description");
        Epic createdEpic = taskManager.createEpic(epic);

        taskManager.createSubtask(new Subtask("Test Subtask 1", "Description 1", createdEpic.getId()));
        taskManager.createSubtask(new Subtask("Test Subtask 2", "Description 2", createdEpic.getId()));


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(200, response.statusCode(), "Ожидается код 200 при успешном получении списка подзадач");
        assertNotNull(response.body(), "Тело ответа не должно быть null");
        assertFalse(response.body().isEmpty(), "Тело ответа не должно быть пустым");
        assertTrue(response.body().contains("Test Subtask 1"), "Ответ должен содержать 'Test Subtask 1'");
        assertTrue(response.body().contains("Test Subtask 2"), "Ответ должен содержать 'Test Subtask 2'");
    }

    @Test
    public void testGetSubtaskById_Success() throws Exception {

        Epic epic = new Epic("Test Epic", "Description");
        Epic createdEpic = taskManager.createEpic(epic);

        Subtask subtaskToCreate = new Subtask("Test Subtask", "Description", createdEpic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtaskToCreate);
        assertNotNull(createdSubtask, "Подзадача должна быть успешно создана");
        int subtaskId = createdSubtask.getId();


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(200, response.statusCode(), "Ожидается код 200 при успешном получении подзадачи по ID");
        assertNotNull(response.body(), "Тело ответа не должно быть null");
        assertFalse(response.body().isEmpty(), "Тело ответа не должно быть пустым");
        assertTrue(response.body().contains("Test Subtask"), "Ответ должен содержать 'Test Subtask'");
    }

    @Test
    public void testGetSubtaskById_NotFound() throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999999")) // Несуществующий ID
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(404, response.statusCode(), "Ожидается код 404 при запросе несуществующей подзадачи");
    }

    @Test
    public void testUpdateSubtask_Success() throws Exception {

        Epic epic = new Epic("Test Epic", "Description");
        Epic createdEpic = taskManager.createEpic(epic);

        Subtask subtaskToCreate = new Subtask("Original Subtask", "Original Description", createdEpic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtaskToCreate);
        assertNotNull(createdSubtask, "Подзадача должна быть успешно создана");
        int originalId = createdSubtask.getId();


        Subtask updatedSubtask = new Subtask("Updated Subtask", "Updated Description", createdEpic.getId());
        updatedSubtask.setId(originalId); // Устанавливаем ID для обновления
        updatedSubtask.setStatus(TaskStatus.IN_PROGRESS); // Меняем статус

        String jsonBody = gson.toJson(updatedSubtask);


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + originalId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)) // Используем POST, как в вашем обработчике
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(200, response.statusCode(), "Ожидается код 200 при успешном обновлении подзадачи");
        assertNotNull(response.body(), "Тело ответа не должно быть null");
        assertFalse(response.body().isEmpty(), "Тело ответа не должно быть пустым");


        Subtask subtaskFromManager = taskManager.getSubtask(originalId);
        assertNotNull(subtaskFromManager, "Обновленная подзадача должна существовать в менеджере");
        assertEquals("Updated Subtask", subtaskFromManager.getTitle(), "Название должно быть обновлено");
        assertEquals("Updated Description", subtaskFromManager.getDescription(), "Описание должно быть обновлено");
        assertEquals(TaskStatus.IN_PROGRESS, subtaskFromManager.getStatus(), "Статус должен быть обновлен");
    }

    @Test
    public void testDeleteSubtask_Success() throws Exception {

        Epic epic = new Epic("Test Epic", "Description");
        Epic createdEpic = taskManager.createEpic(epic);

        Subtask subtaskToCreate = new Subtask("Subtask to Delete", "Description", createdEpic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtaskToCreate);
        assertNotNull(createdSubtask, "Подзадача должна быть успешно создана");
        int subtaskId = createdSubtask.getId();


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtaskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(200, response.statusCode(), "Ожидается код 200 при успешном удалении подзадачи");



        assertNull(taskManager.getSubtask(subtaskId), "Подзадача должна быть удалена из менеджера");
    }

    @Test
    public void testDeleteSubtask_NotFound() throws Exception {
        // Act
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(404, response.statusCode(), "Ожидается код 404 при удалении несуществующей подзадачи");
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


        assertEquals(200, response.statusCode(), "Ожидается код 200 при успешном получении подзадач эпика");
        assertNotNull(response.body(), "Тело ответа не должно быть null");
        assertFalse(response.body().isEmpty(), "Тело ответа не должно быть пустым");
        assertTrue(response.body().contains("Subtask 1 for Epic"), "Ответ должен содержать 'Subtask 1 for Epic'");
        assertTrue(response.body().contains("Subtask 2 for Epic"), "Ответ должен содержать 'Subtask 2 for Epic'");
    }

    @Test
    public void testGetSubtasksByEpicId_EpicNotFound() throws Exception {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/epic/999999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(404, response.statusCode(), "Ожидается код 404 при запросе подзадач несуществующего эпика");
    }
}