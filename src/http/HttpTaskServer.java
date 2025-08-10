package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.handler.*;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new com.google.gson.TypeAdapter<Duration>() {
                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, Duration value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.toString());
                        }
                    }

                    @Override
                    public Duration read(com.google.gson.stream.JsonReader in) throws IOException {
                        String value = in.nextString();
                        if (value == null || value.isEmpty()) {
                            return null;
                        }
                        try {
                            return Duration.parse(value);
                        } catch (Exception e) {
                            System.err.println("Gson (HttpTaskServer): Ошибка парсинга Duration: " + value + ". Причина: " + e.getMessage());
                            return null;
                        }
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new com.google.gson.TypeAdapter<LocalDateTime>() {
                    private final java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.format(formatter));
                        }
                    }

                    @Override
                    public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
                        String value = in.nextString();
                        if (value == null || value.isEmpty()) {
                            return null;
                        }
                        try {
                            return LocalDateTime.parse(value, formatter);
                        } catch (Exception e) {
                            System.err.println("Gson (HttpTaskServer): Ошибка парсинга LocalDateTime: " + value + ". Причина: " + e.getMessage());
                            return null;
                        }
                    }
                })
                .setPrettyPrinting()
                .create();

        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

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

    public Gson getGson() {
        return gson;
    }

    public static void main(String[] args) {
        try {
            TaskManager taskManager = manager.Managers.getDefault();
            if (taskManager == null) {
                System.err.println("Ошибка: Не удалось получить экземпляр TaskManager.");
                return;
            }

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