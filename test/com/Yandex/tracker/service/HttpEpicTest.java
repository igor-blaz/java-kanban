package com.yandex.tracker.service;

import com.google.gson.*;
import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpEpicTest {
    private static final int PORT = 8080;
    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson;
    HttpClient client = HttpClient.newHttpClient();


    @BeforeEach
    public void setUp() throws IOException {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
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
    public void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW);
        epic.setId(99);
        epic.setStartTime(LocalDateTime.now());
        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> tasksFromManager = manager.getEpics();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW);
        epic.setId(99);
        epic.setStartTime(LocalDateTime.now());
        manager.addNewEpic(epic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/99");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Epic> tasksFromManager = manager.getEpics();
        byte[] bytes = response.body().getBytes();

        String body = new String(bytes, StandardCharsets.UTF_8);
        Epic taskFromResponse = gson.fromJson(body, Epic.class);
        assertEquals(200, response.statusCode());
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(taskFromResponse, manager.getEpic(99), "Сервер не дал корректный ответ");
        assertEquals(taskFromResponse.getName(), "Test 2", "Некорректное имя задачи");
    }

    @Test
    void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 1", "Testing epic 1", TaskStatus.NEW);
        epic.setId(999);
        epic.setStartTime(LocalDateTime.now());
        Epic taskTwo = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW);
        taskTwo.setId(1907);
        taskTwo.setStartTime(LocalDateTime.now());
        manager.addNewEpic(epic);
        manager.addNewEpic(taskTwo);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        byte[] bytes = response.body().getBytes();

        String body = new String(bytes, StandardCharsets.UTF_8);
        List<Task> taskArray = gson.fromJson(body, new TaskListTypeToken().getType());
        assertEquals(200, response.statusCode());
        assertEquals(taskArray.get(0).getName(), taskTwo.getName(), "Задачи не возвращаются");
        assertEquals(taskArray.get(1).getName(), epic.getName(), "Задачи не возвращаются");
        assertEquals(taskArray.size(), manager.getEpics().size(), "Массив задач не вернулся корректно");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW);
        epic.setId(99);
        manager.addNewEpic(epic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/99");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> tasksFromManager = manager.getEpics();
        assertTrue(tasksFromManager.isEmpty(), "Задача не удалилась");
    }

    @Test
    void notFoundTest() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/1337");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        byte[] bytes = response.body().getBytes();
        String body = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(404, response.statusCode());
        assertEquals("Not Found", body, "404 ошибка не возникает, когда нет id");
    }

    @Test
    void getSubtasksFromEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing epic 2", TaskStatus.NEW);
        epic.setId(300);
        Subtask subtask = new Subtask("Test 2", "Testing epic 2", TaskStatus.NEW, epic.getId());
        subtask.setId(12);
        manager.addNewSubtask(subtask);
        manager.addNewEpic(epic);
        epic.addEpicSubtask(12);


        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/300/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
        JsonArray subtaskIdsArray = jsonObject.getAsJsonArray("subtaskIds");
        List<Integer> subtaskIds = new ArrayList<>();

        for (JsonElement element : subtaskIdsArray) {
            subtaskIds.add(element.getAsInt());
        }
        assertEquals(200, response.statusCode());
        assertEquals(12, subtaskIds.get(0));

    }
}

