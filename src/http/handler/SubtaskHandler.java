package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/subtasks")) {
                switch (method) {
                    case "GET":
                        handleGetAllSubtasks(exchange);
                        break;
                    case "POST":
                        handleCreateOrUpdateSubtask(exchange);
                        break;
                    case "DELETE":
                        handleDeleteAllSubtasks(exchange);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, -1);
                        exchange.close();
                        break;
                }
            } else if (path.startsWith("/subtasks/") && path.split("/").length >= 3) {
                String[] pathParts = path.split("/");
                try {
                    Integer.parseInt(pathParts[2]);
                    switch (method) {
                        case "GET":
                            handleGetSubtaskById(exchange, path);
                            break;
                        case "POST":
                            handleCreateOrUpdateSubtask(exchange, path);
                            break;
                        case "DELETE":
                            handleDeleteSubtaskById(exchange, path);
                            break;
                        default:
                            exchange.sendResponseHeaders(405, -1);
                            exchange.close();
                            break;
                    }
                } catch (NumberFormatException e) {
                    if (pathParts.length == 4 && "epic".equals(pathParts[2])) {
                        if ("GET".equals(method)) {
                            handleGetSubtasksByEpicId(exchange, path);
                        } else {
                            exchange.sendResponseHeaders(405, -1);
                            exchange.close();
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                sendNotFound(exchange);
            } else {
                sendInternalError(exchange, e);
            }
        } catch (Exception e) {
            sendInternalError(exchange, e);
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        sendText(exchange, subtasks, 200);
    }

    private void handleGetSubtaskById(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            Subtask subtask = taskManager.getSubtask(id);
            if (subtask != null) {
                sendText(exchange, subtask, 200);
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendText(exchange, "Неверный формат ID", 400);
        }
    }

    private void handleGetSubtasksByEpicId(HttpExchange exchange, String path) throws IOException {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length == 4) {
                int epicId = Integer.parseInt(pathParts[3]);
                Epic epic = taskManager.getEpic(epicId);
                if (epic == null) {
                    sendNotFound(exchange);
                    return;
                }
                List<Subtask> subtasks = taskManager.getSubtasksByEpic(epicId);
                sendText(exchange, subtasks, 200);
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendText(exchange, "Неверный формат ID эпика", 400);
        }
    }

    private void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException {
        handleCreateOrUpdateSubtask(exchange, null);
    }

    private void handleCreateOrUpdateSubtask(HttpExchange exchange, String path) throws IOException {
        try {
            String body = extractRequestBody(exchange);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (subtask == null) {
                sendText(exchange, "Тело запроса не содержит корректный JSON подзадачи", 400);
                return;
            }

            boolean isUpdate = path != null && path.startsWith("/subtasks/");
            if (isUpdate) {
                try {
                    int idFromPath = extractIdFromPath(path);
                    subtask.setId(idFromPath);
                } catch (NumberFormatException e) {
                    sendText(exchange, "Неверный формат ID в пути", 400);
                    return;
                }
            }

            Subtask resultSubtask;
            if (isUpdate) {
                if (taskManager.updateSubtask(subtask)) {
                    resultSubtask = taskManager.getSubtask(subtask.getId());
                    taskManager.updateEpicStatus(subtask.getEpicId());
                    taskManager.updateEpicTime(subtask.getEpicId());
                    sendText(exchange, resultSubtask, 200);
                } else {
                    Subtask existingSubtask = taskManager.getSubtask(subtask.getId());
                    if (existingSubtask == null) {
                        sendNotFound(exchange);
                    } else {
                        sendHasInteractions(exchange);
                    }
                }
            } else {
                resultSubtask = taskManager.createSubtask(subtask);
                if (resultSubtask != null) {
                    sendText(exchange, resultSubtask, 201);
                } else {
                    sendHasInteractions(exchange);
                }
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            sendText(exchange, "Некорректный формат JSON", 400);
        }
    }

    private void handleDeleteAllSubtasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllSubtasks();
        sendText(exchange, "Все подзадачи удалены", 200);
    }

    private void handleDeleteSubtaskById(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            if (taskManager.deleteSubtask(id)) {
                sendText(exchange, "Подзадача удалена", 200);
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendText(exchange, "Неверный формат ID", 400);
        }
    }
}