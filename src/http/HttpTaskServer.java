
package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.handler.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Регистрируем обработчики для всех путей
        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        server.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен.");
    }

    public static void main(String[] args) {
        try {
            TaskManager taskManager = Managers.getDefault();
            HttpTaskServer server = new HttpTaskServer(taskManager);
            server.start();
        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ошибка инициализации менеджера задач: " + e.getMessage());
            e.printStackTrace();
        }
    }
}