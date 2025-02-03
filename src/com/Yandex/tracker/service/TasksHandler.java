package com.yandex.tracker.service;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tracker.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {


    private final TaskManager manager;
    private final Gson gson;
    BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int id = -1;
        String[] parts = exchange.getRequestURI().getPath().split("/");
        String method = exchange.getRequestMethod();
        if (parts.length >= 3) {
            try {
                id = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                baseHttpHandler.sendNotFound(exchange);
                return;
            }

        }
        tasksHandler(exchange, id, method);
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
}
