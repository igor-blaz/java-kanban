package com.yandex.tracker.service;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendOk(HttpExchange h) throws IOException {
        String text = "OK";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        String text = "Not Found";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        String text = "Not Acceptable";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(HttpURLConnection.HTTP_NOT_ACCEPTABLE, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendInternalServerError(HttpExchange h) throws IOException {
        String text = "Internal Server Error";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendBadRequest(HttpExchange h) throws IOException {
        String text = "Bad Request";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }
}
