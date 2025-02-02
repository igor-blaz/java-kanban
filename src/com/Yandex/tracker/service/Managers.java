package com.yandex.tracker.service;


import java.io.File;

public class Managers {


    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
    public static FileBackedTaskManager getDefaultFileBacked(File file) {
        return new FileBackedTaskManager(file);
    }

}
