package com.Yandex.tracker.model;


import java.util.ArrayList;


public class Epic extends Task {
    private ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
    }

    public ArrayList<Integer> getEpicSubtasks() {
        return subtaskIds;
    }

    public void setEpicSubtasks(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void addEpicSubtask(int id) {
        subtaskIds.add(id);
    }

    public int getId() {
        return super.getId();
    }


}

