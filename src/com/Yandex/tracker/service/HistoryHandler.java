package com.yandex.tracker.service;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;
    BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

    public HistoryHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        historyHandler(exchange, method);
    }

    private void historyHandler(HttpExchange exchange, String method) throws IOException {
        if (method.equals("GET")) {
            InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();
            baseHttpHandler.sendText(exchange, gson.toJson(inMemoryHistoryManager.getHistory()));
        } else {
            baseHttpHandler.sendBadRequest(exchange);
        }
    }

}
