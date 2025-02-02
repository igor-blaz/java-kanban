package com.yandex.tracker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer implements HttpHandler {
    private static final int PORT = 8080;
    BaseHttpHandler baseHttpHandler = new BaseHttpHandler();
    private final TaskManager manager;
    Gson gson;


    private HttpServer server;

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
    }

    public static void main(String[] args) throws IOException {

        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", this);
        server.createContext("/epics", this);
        server.createContext("/subtasks", this);
        server.createContext("/history", this);
        server.createContext("/prioritized", this);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.setPrettyPrinting();
        this.gson = gsonBuilder.create();
        server.start();
    }

    public void stop(HttpTaskServer server) {
        if (server != null) {
            this.server.stop(1);
        }
    }

    private void epicsHandler(HttpExchange exchange, int id, String method, boolean hasSubtasks) throws IOException {
        String serialized;
        switch (method) {
            case "GET" -> {
                if (manager.getEpic(id) == null && id >= 0 && !hasSubtasks) {
                    baseHttpHandler.sendNotFound(exchange);
                    return;

                } else if (id == -1 && !hasSubtasks) {
                    serialized = gson.toJson(manager.getEpics());
                } else if (id != -1 && !hasSubtasks) {
                    serialized = gson.toJson(manager.getEpic(id));
                } else if (id != -1 && hasSubtasks) {
                    serialized = gson.toJson(manager.getEpic(id).getEpicSubtasks());
                } else {
                    baseHttpHandler.sendNotFound(exchange);
                    return;
                }
                baseHttpHandler.sendText(exchange, serialized);
            }
            case "POST" -> {
                InputStream is = exchange.getRequestBody();
                byte[] bytes = is.readAllBytes();
                String body = new String(bytes, StandardCharsets.UTF_8);
                Epic epic = gson.fromJson(body, Epic.class);
                if (epic == null) {
                    baseHttpHandler.sendBadRequest(exchange);
                    return;
                }
                try {
                    manager.addNewEpic(epic);
                    baseHttpHandler.sendOk(exchange);
                } catch (ManagerSaveException e) {
                    baseHttpHandler.sendInternalServerError(exchange);
                }
            }

            case "DELETE" -> {
                if (manager.getEpic(id) == null) {
                    baseHttpHandler.sendBadRequest(exchange);
                    return;
                }
                manager.deleteEpic(id);
                baseHttpHandler.sendText(exchange, "задача успешно удалена");
            }
            default -> baseHttpHandler.sendBadRequest(exchange);
        }

    }


    private void tasksHandler(HttpExchange exchange, int id, String method) throws IOException {
        String serialized;
        switch (method) {
            case "GET" -> {
                if (manager.getTask(id) == null && id >= 0) {
                    baseHttpHandler.sendNotFound(exchange);
                    return;
                } else if (id == -1) {
                    serialized = gson.toJson(manager.getTasks());
                } else if (manager.getTask(id) != null) {
                    serialized = gson.toJson(manager.getTask(id));
                } else {
                    baseHttpHandler.sendNotFound(exchange);
                    return;
                }
                baseHttpHandler.sendText(exchange, serialized);
            }
            case "POST" -> {
                InputStream is = exchange.getRequestBody();
                byte[] bytes = is.readAllBytes();
                String body = new String(bytes, StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);
                if (id == -1 && task != null) {
                    try {
                        manager.addNewTask(task);
                        if (manager.addNewTask(task) == -2) {
                            baseHttpHandler.sendHasInteractions(exchange);
                            return;
                        }
                        baseHttpHandler.sendOk(exchange);
                    } catch (ManagerSaveException e) {
                        baseHttpHandler.sendInternalServerError(exchange);
                    }
                } else if (id != -1 && task != null) {
                    manager.updateTask(task);
                    baseHttpHandler.sendOk(exchange);
                } else {
                    baseHttpHandler.sendBadRequest(exchange);
                }
            }
            case "DELETE" -> {
                if (manager.getTask(id) != null) {
                    manager.deleteTask(id);
                    baseHttpHandler.sendText(exchange, "задача успешно удалена");
                } else {
                    baseHttpHandler.sendBadRequest(exchange);
                }
            }
            default -> baseHttpHandler.sendBadRequest(exchange);
        }
    }

    private void subtasksHandler(HttpExchange exchange, int id, String method) throws IOException {
        String serialized;
        switch (method) {
            case "GET" -> {
                if (manager.getTask(id) == null && id >= 0) {
                    baseHttpHandler.sendNotFound(exchange);
                } else if (id == -1) {
                    serialized = gson.toJson(manager.getSubtasks());
                    baseHttpHandler.sendText(exchange, serialized);
                } else if (manager.getTask(id) != null) {
                    serialized = gson.toJson(manager.getSubtask(id));
                    baseHttpHandler.sendText(exchange, serialized);
                } else {
                    baseHttpHandler.sendNotFound(exchange);
                }

            }
            case "POST" -> {
                InputStream is = exchange.getRequestBody();
                byte[] bytes = is.readAllBytes();
                String body = new String(bytes, StandardCharsets.UTF_8);
                Subtask subtask = gson.fromJson(body, Subtask.class);
                if (id == -1 && subtask != null) {
                    try {
                        manager.addNewSubtask(subtask);
                        if (manager.addNewSubtask(subtask) == -2) {
                            baseHttpHandler.sendHasInteractions(exchange);
                            return;
                        }
                        baseHttpHandler.sendOk(exchange);
                    } catch (ManagerSaveException e) {
                        baseHttpHandler.sendInternalServerError(exchange);
                    }
                } else if (id != -1 && subtask != null) {
                    manager.updateTask(subtask);
                    baseHttpHandler.sendOk(exchange);
                } else {
                    baseHttpHandler.sendBadRequest(exchange);
                }
            }
            case "DELETE" -> {
                if (manager.getSubtasks().contains(manager.getSubtask(id))) {
                    manager.deleteSubtask(id);
                    baseHttpHandler.sendText(exchange, "задача успешно удалена");
                } else {
                    baseHttpHandler.sendBadRequest(exchange);
                }
            }
            default -> baseHttpHandler.sendBadRequest(exchange);
        }
    }

    private void historyHandler(HttpExchange exchange, String method) throws IOException {
        if (method.equals("GET")) {
            InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();
            baseHttpHandler.sendText(exchange, gson.toJson(inMemoryHistoryManager.getHistory()));
        } else {
            baseHttpHandler.sendBadRequest(exchange);
        }
    }

    private void prioritizedHandler(HttpExchange exchange, String method) throws IOException {
        if (manager.getPrioritizedTasks().isEmpty()) {
            baseHttpHandler.sendText(exchange, "Нет задач со временем");
        } else if (method.equals("GET")) {
            baseHttpHandler.sendText(exchange, gson.toJson(manager.getPrioritizedTasks()));
        } else {
            baseHttpHandler.sendBadRequest(exchange);
        }
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int id = -1;
        boolean hasSubtasks = false;
        String[] parts = exchange.getRequestURI().getPath().split("/");
        String handlerType = parts[1];
        String method = exchange.getRequestMethod();
        if (parts.length >= 3) {
            try {
                id = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                baseHttpHandler.sendNotFound(exchange);
                return;
            }
        }
        if (parts.length == 5 && parts[4].equals("subtasks")) {
            hasSubtasks = true;
        }
        switch (handlerType) {
            case "tasks": {
                tasksHandler(exchange, id, method);
                break;
            }
            case "subtasks": {
                subtasksHandler(exchange, id, method);
                break;
            }
            case "epics": {
                epicsHandler(exchange, id, method, hasSubtasks);
                break;
            }
            case "history": {
                historyHandler(exchange, method);
                break;
            }
            case "prioritized": {
                prioritizedHandler(exchange, method);
                break;
            }
            default:
                baseHttpHandler.sendNotFound(exchange);
        }


    }
}
