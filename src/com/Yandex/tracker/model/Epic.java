package com.yandex.tracker.model;

import java.util.ArrayList;


public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
    }

    public ArrayList<Integer> getEpicSubtasks() {
        return subtaskIds;
    }


    public void addEpicSubtask(int id) {
        subtaskIds.add(id);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }
}

