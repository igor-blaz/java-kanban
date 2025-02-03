package com.yandex.tracker.service;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tracker.model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {


    private final TaskManager manager;
    private final Gson gson;
    BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

    public SubtasksHandler(TaskManager manager, Gson gson) {
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
        subtasksHandler(exchange, id, method);
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
}
