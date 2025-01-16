package com.yandex.tracker.model;


import com.yandex.tracker.service.FileBackedTaskManager;
import com.yandex.tracker.service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;


public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds = new ArrayList<>();
    private final InMemoryTaskManager manager = new InMemoryTaskManager();

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
    }

    public ArrayList<Integer> getEpicSubtasks() {
        return subtaskIds;
    }

    public LocalDateTime getOldestStart() {
        return subtaskIds.stream().
                map(manager::getSubtask).
                map(Subtask::getStart).
                min(Comparator.naturalOrder()).
                orElse(null);

    }

    public LocalDateTime getNewestFinish() {
        return subtaskIds.stream().
                map(manager::getSubtask).
                map(Subtask::getFinish).
                max(Comparator.naturalOrder()).
                orElse(null);

    }

    public void setTimeForEpic() {
        if (subtaskIds.size()>=2) {
            setStartTime(getOldestStart());
            setFinishTime(getNewestFinish());
            if (getOldestStart() != null && getNewestFinish() != null) {
                setDuration(Duration.between(getOldestStart(), getNewestFinish()));
            }
        } else {
            setDuration(Duration.ZERO);
            setStartTime(null);
            setFinishTime(null);
        }
    }

    public void addEpicSubtask(int id) {
        subtaskIds.add(id);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }
}

