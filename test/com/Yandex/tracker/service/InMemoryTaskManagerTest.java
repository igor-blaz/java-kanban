package com.yandex.tracker.service;

import com.yandex.tracker.model.Epic;
import com.yandex.tracker.model.Subtask;
import com.yandex.tracker.model.Task;
import com.yandex.tracker.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    Epic taskEpic;
    Subtask task1;
    Subtask task2;
    Subtask task3;


    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }


    @BeforeEach
    public void setUp() {
        createTaskManager();

        taskManager = createTaskManager();


        taskManager.deleteEpics();
        taskManager.deleteSubtasks();
        taskManager.deleteTasks();
        taskEpic = new Epic("Epic_Epic", "Test description", TaskStatus.IN_PROGRESS);
        taskEpic.setId(700);
        taskManager.addNewEpic(taskEpic);
        task1 = new Subtask("Test", " description", TaskStatus.NEW, taskEpic.getId());
        task2 = new Subtask("Test", " description", TaskStatus.NEW, taskEpic.getId());
        task3 = new Subtask("Test ", " description", TaskStatus.NEW, taskEpic.getId());

        taskManager.addNewSubtask(task1);
        taskManager.addNewSubtask(task2);
        taskManager.addNewSubtask(task3);

    }

    @Test
    void allSubtaskIsNew() {
        assertEquals(taskEpic.getStatus(), TaskStatus.NEW, "Статус не обновился");
    }

    @Test
    void allSubtaskIsDone() {
        task1.setStatus(TaskStatus.DONE);
        task2.setStatus(TaskStatus.DONE);
        task3.setStatus(TaskStatus.DONE);
        taskManager.addNewSubtask(task1);
        taskManager.addNewSubtask(task2);
        taskManager.addNewSubtask(task3);
        assertEquals(taskEpic.getStatus(), TaskStatus.DONE, "Статус не обновился");
    }

    @Test
    void SubtaskIsNewAndDone() {
        task1.setStatus(TaskStatus.NEW);
        task2.setStatus(TaskStatus.DONE);
        task3.setStatus(TaskStatus.NEW);
        taskManager.addNewSubtask(task1);
        taskManager.addNewSubtask(task2);
        taskManager.addNewSubtask(task3);
        assertEquals(taskEpic.getStatus(), TaskStatus.IN_PROGRESS, "Статус не обновился");
    }

    @Test
    void SubtaskInProgress() {
        task1.setStatus(TaskStatus.IN_PROGRESS);
        task2.setStatus(TaskStatus.DONE);
        task3.setStatus(TaskStatus.NEW);
        taskManager.addNewSubtask(task1);
        taskManager.addNewSubtask(task2);
        taskManager.addNewSubtask(task3);
        assertEquals(taskEpic.getStatus(), TaskStatus.IN_PROGRESS, "Статус не обновился");
    }

    @Test
    void createCrossTimeTest() {
        Task taskOne = new Task("Test addNewTask1", "Test addNewTask description", TaskStatus.NEW);
        taskOne.setId(8);
        taskOne.setStartTime(LocalDateTime.of(2000, 1, 1, 10, 0));
        taskOne.setFinishTime(LocalDateTime.of(2000, 1, 10, 10, 0));
        Task taskTwo = new Task("Test addNewTask2", "Test addNewTask description", TaskStatus.NEW);
        taskTwo.setId(2);
        taskTwo.setStartTime(LocalDateTime.of(2000, 1, 8, 10, 0));
        taskTwo.setFinishTime(LocalDateTime.of(2000, 1, 18, 10, 0));

        taskManager.addNewTask(taskOne);
        taskManager.addNewTask(taskTwo);
        assertEquals(taskManager.prioritizedTasks.size(), 2, "Не добавились задачи со временем.");
        assertTrue(taskManager.isCrossTime(taskTwo), "Не получилось найти пересечение.");


    }

    @Test
    void createTaskTest() {

        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        final int taskId = taskManager.addNewTask(task);

        Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");

    }

    @Test
    void createAndRetrieveEpicWithSubtasksTest() {

        taskManager.deleteSubtasks();
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description", TaskStatus.NEW);
        epic.setId(12);
        taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.NEW, epic.getId());
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
        epic.setId(50);
        taskManager.addNewEpic(epic);

        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.NEW, epic.getId());
        subtask.setId(23);
        taskManager.addNewSubtask(subtask);
        taskManager.deleteSubtask(subtask.getId());
        assertNull(taskManager.getSubtask(subtask.getId()), "Задача не удалена");
    }

    @Test
    void deleteAllSubtasksByEpicTest() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description", TaskStatus.IN_PROGRESS);

        final int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.NEW, epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask);
        taskManager.deleteSubtasks();
        assertNull(taskManager.getSubtask(subtaskId), "Задача не удалена");
    }

    @Test
    void deleteAllEpicsTest() {
        Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description", TaskStatus.IN_PROGRESS);

        final int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", TaskStatus.NEW, epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask);
        taskManager.deleteEpics();
        assertNull(taskManager.getEpic(subtaskId), "Задача не удалена");
    }


}