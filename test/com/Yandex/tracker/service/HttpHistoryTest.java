package com.yandex.tracker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpHistoryTest {
    private static final int PORT = 8080;
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson;


    @BeforeEach
    public void setUp() throws IOException {

        taskServer.start();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.setPrettyPrinting();
        this.gson = gsonBuilder.create();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop(taskServer);
    }

    @Test
    public void getHistory() throws IOException, InterruptedException {
        InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();

        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task.setId(9);
        inMemoryHistoryManager.add(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }
}
