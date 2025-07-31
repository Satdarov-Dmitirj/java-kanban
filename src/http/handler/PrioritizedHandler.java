
package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && "/prioritized".equals(path)) {
                handleGetPrioritizedTasks(exchange);
            } else {

                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        } catch (Exception e) {
            sendInternalError(exchange, e);
        }
    }

    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {

        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        sendText(exchange, prioritizedTasks, 200); // 200 OK
    }
}