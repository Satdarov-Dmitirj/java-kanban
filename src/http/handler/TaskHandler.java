package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/tasks")) {
                switch (method) {
                    case "GET":
                        handleGetAllTasks(exchange);
                        break;
                    case "POST":
                        handleCreateOrUpdateTask(exchange);
                        break;
                    case "DELETE":
                        handleDeleteAllTasks(exchange);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, -1);
                        exchange.close();
                        break;
                }
            } else if (path.startsWith("/tasks/")) {
                switch (method) {
                    case "GET":
                        handleGetTaskById(exchange, path);
                        break;
                    case "POST":
                        handleCreateOrUpdateTask(exchange, path);
                        break;
                    case "DELETE":
                        handleDeleteTaskById(exchange, path);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, -1);
                        exchange.close();
                        break;
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange, e);
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        sendText(exchange, tasks, 200);
    }

    private void handleGetTaskById(HttpExchange exchange, String path) throws IOException {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                Task task = taskManager.getTask(id);
                if (task != null) {
                    sendText(exchange, task, 200);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendText(exchange, "Неверный формат ID", 400);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                sendNotFound(exchange);
            } else {
                throw e;
            }
        }
    }

    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        handleCreateOrUpdateTask(exchange, null);
    }

    private void handleCreateOrUpdateTask(HttpExchange exchange, String path) throws IOException {
        try {
            String body = extractRequestBody(exchange);
            Task task = gson.fromJson(body, Task.class);
            if (task == null) {
                sendText(exchange, "Тело запроса не содержит корректный JSON задачи", 400);
                return;
            }

            boolean isUpdate = path != null && path.startsWith("/tasks/");
            if (isUpdate) {
                try {
                    int idFromPath = extractIdFromPath(path);
                    task.setId(idFromPath);
                } catch (NumberFormatException e) {
                    sendText(exchange, "Неверный формат ID в пути", 400);
                    return;
                }
            }

            Task resultTask;
            if (isUpdate) {
                if (taskManager.updateTask(task)) {
                    resultTask = taskManager.getTask(task.getId());
                    sendText(exchange, resultTask, 200);
                } else {
                    Task existingTask = taskManager.getTask(task.getId());
                    if (existingTask == null) {
                        sendNotFound(exchange);
                    } else {
                        sendHasInteractions(exchange);
                    }
                }
            } else {
                resultTask = taskManager.createTask(task);
                if (resultTask != null) {
                    sendText(exchange, resultTask, 201);
                } else {
                    sendHasInteractions(exchange);
                }
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            sendText(exchange, "Некорректный формат JSON", 400);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                sendNotFound(exchange);
            } else {
                sendInternalError(exchange, e);
            }
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllTasks();
        sendText(exchange, "Все задачи удалены", 200);
    }

    private void handleDeleteTaskById(HttpExchange exchange, String path) throws IOException {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                if (taskManager.deleteTask(id)) {
                    sendText(exchange, "Задача удалена", 200);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendText(exchange, "Неверный формат ID", 400);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                sendNotFound(exchange);
            } else {
                throw e;
            }
        }
    }
}