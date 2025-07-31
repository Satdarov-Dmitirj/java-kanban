
package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method) && "/history".equals(path)) {
                handleGetHistory(exchange);
            } else {

                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        } catch (Exception e) {
            sendInternalError(exchange, e);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {

        List<Task> history = taskManager.getHistory();

        sendText(exchange, history, 200);
    }
}