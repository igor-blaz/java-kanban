package com.yandex.tracker.service;

import com.yandex.tracker.model.*;
import com.yandex.tracker.service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;

    // Абстрактный метод для создания экземпляра конкретного TaskManager
    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
        taskManager.deleteTasks();
        taskManager.deleteEpics();
        taskManager.deleteSubtasks();
    }

    @Test
    void addNewTaskTest() {
        Task task = new Task("Test Task", "Description", TaskStatus.NEW);
        int taskId = taskManager.addNewTask(task);
        assertNotNull(taskManager.getTask(taskId));
        assertEquals(task, taskManager.getTask(taskId));
    }

    @Test
    void deleteTaskByIdTest() {
        Task task = new Task("Test Task", "Description", TaskStatus.NEW);
        int taskId = taskManager.addNewTask(task);
        taskManager.deleteTask(taskId);
        assertNull(taskManager.getTask(taskId));
    }

    @Test
    void updateTaskTest() {
        Task task = new Task("Test Task", "Description", TaskStatus.NEW);
        int taskId = taskManager.addNewTask(task);
        Task updatedTask = new Task("Updated Task",  "Description", TaskStatus.IN_PROGRESS);
        updatedTask.setId(taskId);
        taskManager.updateTask(updatedTask);
        assertEquals(updatedTask, taskManager.getTask(taskId));
    }

    @Test
    void getTasksTest() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        assertEquals(2, taskManager.getTasks().size());
    }

    @Test
    void addNewEpicTest() {
        Epic epic = new Epic("Test Epic", "Epic Description", TaskStatus.NEW);
        int epicId = taskManager.addNewEpic(epic);
        assertNotNull(taskManager.getEpic(epicId));
        assertEquals(epic, taskManager.getEpic(epicId));
    }

    @Test
    void getEpicsTest() {
        Epic epic1 = new Epic("Epic 1", "Description 1", TaskStatus.NEW);
        Epic epic2 = new Epic("Epic 2", "Description 2", TaskStatus.NEW);
        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);
        assertEquals(2, taskManager.getEpics().size());
    }

    @Test
    void addNewSubtaskTest() {
        Epic epic = new Epic("Test Epic", "Epic Description", TaskStatus.NEW);
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.addNewSubtask(subtask);
        assertNotNull(taskManager.getSubtask(subtaskId));
        assertEquals(subtask, taskManager.getSubtask(subtaskId));
    }

    

    @Test
    void clearAllDataTest() {
        taskManager.addNewTask(new Task("Task 1", "Description 1", TaskStatus.NEW));
        taskManager.addNewEpic(new Epic("Epic 1", "Description 1", TaskStatus.NEW));
        taskManager.deleteTasks();
        taskManager.deleteEpics();
        taskManager.deleteSubtasks();
        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getEpics().isEmpty());
    }
}
