package com.yandex.tracker.service;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;
    BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        prioritizedHandler(exchange, method);
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
}
