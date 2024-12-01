package com.Yandex.tracker.service;

import com.Yandex.tracker.model.Epic;
import com.Yandex.tracker.model.Subtask;
import com.Yandex.tracker.model.Task;
import com.Yandex.tracker.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

     private TaskManager taskManager;


    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();


        taskManager.deleteEpics();
        taskManager.deleteSubtasks();
        taskManager.deleteTasks();
    }

    @Test
    void createTaskTest() {

            Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
            final int taskId = taskManager.addNewTask(task);

            final Task savedTask = taskManager.getTask(taskId);

            assertNotNull(savedTask, "Задача не найдена.");
            assertEquals(task, savedTask, "Задачи не совпадают.");

            final List<Task> tasks = taskManager.getTasks();

            assertNotNull(tasks, "Задачи не возвращаются.");
            assertEquals(1, tasks.size(), "Неверное количество задач.");
            assertEquals(task, tasks.get(0), "Задачи не совпадают.");

    }

    @Test
    void createAndRetrieveEpicWithSubtasksTest() {


        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description", TaskStatus.NEW);

        final int epicId=  taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.NEW,epicId);
        final int taskId = taskManager.addNewSubtask(subtask);

        final Task savedTask = taskManager.getSubtask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(subtask, savedTask, "Задачи не совпадают.");

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Задачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask, subtasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void updateSubtaskAndEpicStatusTest() {


        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description", TaskStatus.NEW);
        final int epicId=  taskManager.addNewEpic(epic);
        Subtask subtaskOne = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.DONE,epicId);
        final int taskIdOne = taskManager.addNewSubtask(subtaskOne);
        Subtask subtaskTwo = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.DONE,epicId);
        final int taskIdTwo = taskManager.addNewSubtask(subtaskTwo);
        taskManager.updateEpicStatus(epicId);
        assertNotNull(epic, "null вместо задачи");
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус не обновился.");
    }

    @Test
    void updateTaskStatusTest() {

        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus(), "Статус не обновился.");
    }

    @Test
    void deleteTaskByIdTest() {

        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);
        taskManager.deleteTask(taskId);
        assertNull(taskManager.getTask(taskId), "Задача не удалена");
    }

    @Test
    void deleteAllTasksTest() {

        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.DONE);
        final int taskId = taskManager.addNewTask(task);
        Task taskTwo = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskIdTwo = taskManager.addNewTask(taskTwo);
        taskManager.deleteTasks();
        assertNull(taskManager.getTask(taskId), "Задача не удалена");
        assertNull(taskManager.getTask(taskIdTwo), "Задача не удалена");
    }

    @Test
    void deleteSubtaskByIdTest() {


        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description", TaskStatus.NEW);

        final int epicId= taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.NEW,epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask);
        taskManager.deleteSubtask(subtaskId);
        assertNull(taskManager.getSubtask(subtaskId), "Задача не удалена");
    }

    @Test
    void deleteAllSubtasksByEpicTest() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description", TaskStatus.IN_PROGRESS);

        final int epicId= taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.NEW,epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask);
        taskManager.deleteSubtasks();
        assertNull(taskManager.getSubtask(subtaskId), "Задача не удалена");
    }

    @Test
    void deleteAllEpicsTest() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description", TaskStatus.IN_PROGRESS);

        final int epicId= taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.NEW,epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask);
        taskManager.deleteEpics();
        assertNull(taskManager.getEpic(subtaskId), "Задача не удалена");
    }

    @Test
    void changesCantMakeHistoryDifferent() {

        HistoryManager historyManager = Managers.getDefaultHistory();
        Epic task = new Epic("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);
        historyManager.add(task);
        task.setName("непредсказуемое имя");
        task.setId(44);
        task.setStatus(TaskStatus.DONE);
        List<Task> history = historyManager.getHistory();
        Task oldTask = history.get(0);

        assertNotEquals(task.getName(),oldTask.getName(),"Изменения затронули history" );
        assertNotEquals(task.getId(),oldTask.getId(), "Изменения затронули history" );
        assertNotEquals(task.getStatus(),oldTask.getStatus(),"Изменения затронули history" );
    }

}