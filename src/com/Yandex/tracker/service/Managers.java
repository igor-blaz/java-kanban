package com.Yandex.tracker.service;

import com.Yandex.tracker.model.Epic;
import com.Yandex.tracker.model.Subtask;
import com.Yandex.tracker.model.Task;

import java.util.ArrayList;

public class Managers {


    public static TaskManager getDefault() {
        return new InMemoryTaskManager();  // Возвращаем реальную реализацию InMemoryTaskManager
    }

    public static HistoryManager getDefaultHistory()  {

        return new InMemoryHistoryManager();
    }
}
