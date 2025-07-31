
package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected final Gson gson;

    public BaseHttpHandler(Gson gson) {
        this.gson = gson;
    }


    protected void sendText(HttpExchange h, Object object, int statusCode) throws IOException {
        String json = gson.toJson(object);
        byte[] resp = json.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendText(HttpExchange h, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        sendText(h, text, 200);
    }


    protected void sendNotFound(HttpExchange h) throws IOException {
        sendText(h, "Объект не найден", 404);
    }


    protected void sendHasInteractions(HttpExchange h) throws IOException {
        sendText(h, "Задача пересекается с существующими", 406);
    }


    protected void sendInternalError(HttpExchange h, Exception e) throws IOException {
        e.printStackTrace();
        sendText(h, "Внутренняя ошибка сервера", 500);
    }


    protected String extractRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }


    protected int extractIdFromPath(String path) throws NumberFormatException {
        String[] pathParts = path.split("/");
        if (pathParts.length >= 3) {
            return Integer.parseInt(pathParts[2]);
        }
        throw new NumberFormatException("Неверный формат пути для извлечения ID");
    }
}