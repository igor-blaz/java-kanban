package com.Yandex.tracker.service;

import com.Yandex.tracker.model.Epic;
import com.Yandex.tracker.model.Subtask;
import com.Yandex.tracker.model.Task;

import java.util.List;
import java.util.ArrayList;

import com.Yandex.tracker.model.Subtask;

import java.util.ArrayList;


public interface HistoryManager {
    void add(Task task);

    void remove(int id);

    List<Task> getHistory();
}