package com.Yandex.tracker.service;


public class Managers {


    public static TaskManager getDefault() {
        return new InMemoryTaskManager();  // Возвращаем реальную реализацию InMemoryTaskManager
    }

    public static HistoryManager getDefaultHistory() {

        return new InMemoryHistoryManager();
    }
}
