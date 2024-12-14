package com.Yandex.tracker.service;

import com.Yandex.tracker.model.*;

import java.io.*;
import java.nio.file.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path file;

    public FileBackedTaskManager(Path file) {
        this.file = file;
    }

    public void save() {

    }
    @Override
    public int addNewSubtask(Subtask subtask) {
        int id = super.addNewSubtask(subtask);
        save();
        return id;
    }
    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }
    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }
    @Override
    public void deleteTask(int id) {
        save();
    }


}
