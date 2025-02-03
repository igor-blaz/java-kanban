package com.yandex.tracker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskTest {
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
        manager.deleteTasks();
        manager.deleteEpics();
        manager.deleteSubtasks();

    }

    @AfterEach
    public void shutDown() {

        taskServer.stop(taskServer);

    }

    @Test
    public void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task.setId(99);
        task.setStartTime(LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task.setId(99);
        task.setStartTime(LocalDateTime.now());
        manager.addNewTask(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/99");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Task> tasksFromManager = manager.getTasks();
        byte[] bytes = response.body().getBytes();

        String body = new String(bytes, StandardCharsets.UTF_8);
        Task taskFromResponse = gson.fromJson(body, Task.class);
        assertEquals(200, response.statusCode());
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(taskFromResponse, manager.getTask(99), "Сервер не дал корректный ответ");
        assertEquals(taskFromResponse.getName(), "Test 2", "Некорректное имя задачи");
    }

    @Test
    void testGetAllTasks() throws IOException, InterruptedException {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        task.setId(99);
        task.setStartTime(LocalDateTime.now());
        Task taskTwo = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task.setId(107);
        task.setStartTime(LocalDateTime.now());
        manager.addNewTask(task);
        manager.addNewTask(taskTwo);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        byte[] bytes = response.body().getBytes();

        String body = new String(bytes, StandardCharsets.UTF_8);
        List<Task> taskArray = gson.fromJson(body, new TaskListTypeToken().getType());
        assertEquals(200, response.statusCode());
        assertEquals(taskArray.get(0), taskTwo, "Задачи не возвращаются");
        assertEquals(taskArray.get(1), task, "Задачи не возвращаются");
        assertEquals(taskArray.size(), manager.getTasks().size(), "Массив задач не вернулся корректно");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task.setId(99);
        manager.addNewTask(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/99");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertTrue(tasksFromManager.isEmpty(), "Задача не удалилась");
    }

    @Test
    void crossTimeErrorTest() throws IOException, InterruptedException {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        task.setId(99);
        task.setStartTime(LocalDateTime.of(2000, 1, 1, 10, 0));
        task.setFinishTime(LocalDateTime.of(2000, 1, 10, 10, 0));

        Task taskTwo = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task.setId(107);
        taskTwo.setStartTime(LocalDateTime.of(2000, 1, 8, 10, 0));
        taskTwo.setFinishTime(LocalDateTime.of(2000, 1, 18, 10, 0));
        manager.addNewTask(task);
        manager.addNewTask(taskTwo);
        URI url = URI.create("http://localhost:8080/tasks");
        String taskJson = gson.toJson(task);
        String taskJsonTwo = gson.toJson(taskTwo);
        HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpRequest requestTwo = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJsonTwo)).build();
        HttpResponse<String> responseTwo = client.send(requestTwo, HttpResponse.BodyHandlers.ofString());
        byte[] bytes = responseTwo.body().getBytes();

        String body = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(responseTwo.statusCode(), 406, "Задачи пересекаются ");
        assertEquals(body, "Not Acceptable", "ошибка не выбрасывается");
    }

    @Test
    void notFoundTest() throws IOException, InterruptedException {

        URI url = URI.create("http://localhost:8080/tasks/1337");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        System.out.println(HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        byte[] bytes = response.body().getBytes();
        String body = new String(bytes, StandardCharsets.UTF_8);
        assertEquals(404, response.statusCode());
        assertEquals("Not Found", body, "404 ошибка не возникает, когда нет id");
    }


}
