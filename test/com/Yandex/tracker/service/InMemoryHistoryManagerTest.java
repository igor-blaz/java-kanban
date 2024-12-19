package com.Yandex.tracker.service;


import com.Yandex.tracker.model.Task;
import com.Yandex.tracker.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private TaskManager taskManager;
    private InMemoryHistoryManager historyManager;
    private Task taskOne;
    private Task taskTwo;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();

        taskManager = Managers.getDefault();
        taskOne = new Task("имя", "описание", TaskStatus.NEW);
        taskOne.setId(17);

        taskTwo = new Task("имя2", "описание2", TaskStatus.NEW);
        taskTwo.setId(19);
    }

    @Test
    void addToHistoryTest() {
        historyManager.add(taskOne);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Должна быть одна задача");
        assertEquals(taskOne, history.get(0), "Задача в истории должна быть равна добавленной");
    }

    @Test
    void getHistory() {
        historyManager.add(taskOne);
        historyManager.add(taskTwo);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно быть 2 задачи");
        assertTrue(history.contains(taskOne), "Должна быть первая задача");
        assertTrue(history.contains(taskTwo), "Должна быть вторая задача");
    }

    @Test
    void historyAntiRepeatsTest() {
        historyManager.add(taskOne);
        Task taskCopy = new Task("имя", "описание", TaskStatus.NEW);
        taskCopy.setId(17);
        historyManager.add(taskCopy);
        historyManager.add(taskCopy);
        List<Task> history = historyManager.getHistory();


        assertTrue(history.contains(taskCopy), "Новая версия должна быть добавлена");
        assertTrue(history.size() == 1, "Не должно быть повторений");
    }

    @Test
    void historyShouldReturnOnlyNewViewsInCorrectOrder() {
        TaskManager taskManager = Managers.getDefault();
        Task taskFirst = new Task("Первая", "описание", TaskStatus.NEW);
        taskManager.addNewTask(taskFirst);
        Task taskTwo = new Task("Вторая", "описание", TaskStatus.NEW);
        taskManager.addNewTask(taskTwo);
        Task taskThree = new Task("Третья", "описание", TaskStatus.NEW);
        taskManager.addNewTask(taskThree);

        taskManager.getTask(1);
        taskManager.getTask(2);
        taskManager.getTask(3);
        taskManager.getTask(1);

        List<Task> history = taskManager.getHistory();
        Collections.reverse(history);
        assertEquals(3, history.size(), "История должна содержать три задачи");
        assertEquals(taskFirst, history.get(0), "Первая задача должна быть в начале");
        assertEquals(taskThree, history.get(1), "Третья задача должна быть второй");
        assertEquals(taskTwo, history.get(2), "Вторая задача должна быть в конце");
    }

    @Test
    void taskShouldBeDeletedByInformationFromHistory() {
        TaskManager taskManager = Managers.getDefault();
        historyManager.add(taskOne);
        List<Task> historyArray = new ArrayList<>(historyManager.getHistory());
        Task task = historyArray.get(0);
        taskManager.deleteTask(task.getId());
        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "Задача должна быть удалена");
    }
}


