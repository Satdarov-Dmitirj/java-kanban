
package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/epics")) {
                switch (method) {
                    case "GET":
                        handleGetAllEpics(exchange);
                        break;
                    case "POST":
                        handleCreateOrUpdateEpic(exchange);
                        break;
                    case "DELETE":
                        handleDeleteAllEpics(exchange);
                        break;
                    default:
                        exchange.sendResponseHeaders(405, -1);
                        exchange.close();
                        break;
                }
            } else if (path.startsWith("/epics/")) {

                String[] pathParts = path.split("/");
                if (pathParts.length == 3) {
                    switch (method) {
                        case "GET":
                            handleGetEpicById(exchange, path);
                            break;
                        case "POST": // Предполагается обновление
                            handleCreateOrUpdateEpic(exchange, path);
                            break;
                        case "DELETE":
                            handleDeleteEpicById(exchange, path);
                            break;
                        default:
                            exchange.sendResponseHeaders(405, -1);
                            exchange.close();
                            break;
                    }
                } else if (pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
                    if ("GET".equals(method)) {
                        handleGetSubtasksByEpicId(exchange, path);
                    } else {
                        exchange.sendResponseHeaders(405, -1);
                        exchange.close();
                    }
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange, e);
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getAllEpics();
        sendText(exchange, epics, 200);
    }

    private void handleGetEpicById(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            Epic epic = taskManager.getEpic(id);
            if (epic != null) {
                sendText(exchange, epic, 200); // 200 OK
            } else {
                sendNotFound(exchange); // 404 Not Found
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


    private void handleGetSubtasksByEpicId(HttpExchange exchange, String path) throws IOException {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length == 4) {
                int epicId = extractIdFromPath(path);
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
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                sendNotFound(exchange);
            } else {
                throw e;
            }
        }
    }

    private void handleCreateOrUpdateEpic(HttpExchange exchange) throws IOException {
        handleCreateOrUpdateEpic(exchange, null);
    }

    private void handleCreateOrUpdateEpic(HttpExchange exchange, String path) throws IOException {
        try {
            String body = extractRequestBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);
            if (epic == null) {
                sendText(exchange, "Тело запроса не содержит корректный JSON эпика", 400); // 400 Bad Request
                return;
            }

            boolean isUpdate = path != null && path.startsWith("/epics/");
            if (isUpdate) {
                try {
                    int idFromPath = extractIdFromPath(path);
                    epic.setId(idFromPath);
                } catch (NumberFormatException e) {
                    sendText(exchange, "Неверный формат ID в пути", 400); // 400 Bad Request
                    return;
                }
            }

            Epic resultEpic;
            if (isUpdate) {
                if (taskManager.updateEpic(epic)) {
                    taskManager.updateEpicStatus(epic.getId());
                    taskManager.updateEpicTime(epic.getId());
                    resultEpic = taskManager.getEpic(epic.getId());
                    sendText(exchange, resultEpic, 200);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                resultEpic = taskManager.createEpic(epic);
                sendText(exchange, resultEpic, 201); // 201 Created
            }
        } catch (com.google.gson.JsonSyntaxException e) {
            sendText(exchange, "Некорректный формат JSON", 400); // 400 Bad Request
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                sendNotFound(exchange);
            } else {
                throw e;
            }
        }
    }

    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        taskManager.deleteAllEpics();
        sendText(exchange, "Все эпики и связанные подзадачи удалены", 200); // 200 OK
    }

    private void handleDeleteEpicById(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            if (taskManager.deleteEpic(id)) {
                sendText(exchange, "Эпик и связанные подзадачи удалены", 200); // 200 OK
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendText(exchange, "Неверный формат ID", 400); // 400 Bad Request
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("не найден")) {
                sendNotFound(exchange);
            } else {
                throw e;
            }
        }
    }
}
