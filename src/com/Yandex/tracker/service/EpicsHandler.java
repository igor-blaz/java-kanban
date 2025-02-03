package com.yandex.tracker.service;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tracker.model.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;
    BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

    public EpicsHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
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
        epicsHandler(exchange, id, method, hasSubtasks);

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

}
