package com.Yandex.tracker.model;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }
    public int getEpicId() {
        return epicId;
    }
    @Override
    public void setId(int id) {
        super.setId(id);
    }
    @Override
    public int getId() {
        return super.getId();
    }
    @Override
    public TaskStatus getStatus() {
        return super.getStatus();
    }
    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
    }
    @Override
    public String toString() {
        return super.toString() + " | EpicId: " + epicId;
    }
}
