package com.Yandex.tracker.service;

import com.Yandex.tracker.model.Epic;
import com.Yandex.tracker.model.Subtask;
import com.Yandex.tracker.model.Task;


import java.util.List;

public interface TaskManager {
    List<Task> getHistory();

    List<Subtask> getSubtasks();

    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getEpicSubtasks(int epicId);

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    int addNewTask(Task task);

    int addNewEpic(Epic epic);

    Integer addNewSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    void deleteTasks();

    void deleteSubtasks();

    void deleteEpics();

    void updateEpicStatus(int epicId);
}
