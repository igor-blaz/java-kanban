package com.Yandex.tracker.service;


import com.Yandex.tracker.model.Task;

import java.util.List;


public interface HistoryManager {
    void add(Task task);

    void remove(int id);

    List<Task> getHistory();
}