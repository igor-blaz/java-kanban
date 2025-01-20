package com.yandex.tracker.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.yandex.tracker.model.*;

public class InMemoryTaskManager implements TaskManager {
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStart));

    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    private int id;
    private final HistoryManager history = Managers.getDefaultHistory();

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
    }

    private void deletePrioritizedTask(Task task) {

        prioritizedTasks.remove(task);

    }

    public void deleteAllPrioritizedTask() {
        prioritizedTasks.removeIf(task -> task.getClass() == Task.class);
    }

    public void deleteAllPrioritizedEpics() {
        prioritizedTasks.removeIf(task -> task.getClass() == Epic.class ||
                task.getClass() == Subtask.class);
    }

    public void deleteAllPrioritizedSubtasks() {
        prioritizedTasks.removeIf(task -> task.getClass() == Subtask.class);
    }


    private void addToPrioritizedTasks(Task task) {
        if (task.getStart() != null) {
            prioritizedTasks.add(task);
        }
    }

    private boolean isCrossTime(Task task) {
        if (task.getStart() != null && getPrioritizedTasks().size() > 1) {
            return getPrioritizedTasks().stream()
                    .anyMatch(anyTask -> task.getStart()
                            .isBefore(anyTask.getFinish()) &&
                            task.getFinish().isAfter(anyTask.getStart()) ||
                            (anyTask.getStart().isBefore(task.getStart())));
        } else {
            return false;

        }
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(this.tasks.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(this.subtasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(this.epics.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        return Optional.ofNullable(epics.get(epicId))
                .map(epic -> epic.getEpicSubtasks().stream()
                        .map(subtasks::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            history.add(tasks.get(id));
        }
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        history.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public Epic getEpic(int id) {
        history.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public int addNewTask(Task task) {
        if (isCrossTime(task)) {
            System.out.println(9);
            return -1;

        }
        task.setStatus(TaskStatus.NEW);
        if (task.getId() == 0) {
            task.setId(id++);
            tasks.put(id, task);
        } else if (task.getId() != 0) {
            tasks.put(task.getId(), task);
        }
        addToPrioritizedTasks(task);

        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {

        if (epic.getId() == 0) {
            epic.setId(id++);
            epics.put(id, epic);
            return epic.getId();
        }
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (epics.containsKey(epicId)) {
            subtask.setId(id++);
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(epicId);
            epic.addEpicSubtask(subtask.getId());
            if (prioritizedTasks.size() >= 2) {
                setTimeForEpic(epic);
            }
            updateEpicStatus(epicId);
            addToPrioritizedTasks(subtask);
            return subtask.getId();
        }
        return -1;
    }

    private void updatePrioritizedTasks(Task task) {
        if (task.getStart() != null) {
            prioritizedTasks.removeIf(anyTask -> anyTask.getId() == task.getId());
            prioritizedTasks.add(task);

        }
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            updatePrioritizedTasks(task);
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
            updatePrioritizedTasks(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            setTimeForEpic(epic);

        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        deletePrioritizedTask(getTask(id));
        history.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        final Epic epic = epics.remove(id);
        if (epic != null) {
            history.remove(id);
            for (Integer subtaskId : epic.getEpicSubtasks()) {
                subtasks.remove(subtaskId);
                history.remove(subtaskId);
                deletePrioritizedTask(subtasks.get(subtaskId));
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        history.remove(id);
        ArrayList<Integer> subtasksIdForEpics = epic.getEpicSubtasks();
        for (int i = 0; i < subtasksIdForEpics.size(); i++) {
            if (subtasksIdForEpics.get(i) == id) {
                subtasksIdForEpics.remove(i);
                break;
            }
        }

        setTimeForEpic(epic);
        updateEpicStatus(epic.getId());
        deletePrioritizedTask(subtask);
    }

    @Override
    public void deleteTasks() {
        for (Integer taskId : tasks.keySet()) {
            history.remove(taskId);
        }
        deleteAllPrioritizedTask();
        tasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Integer subtaskId : subtasks.keySet()) {
            history.remove(subtaskId);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            setTimeForEpic(epic);
            updateEpicStatus(epic.getId());
        }
        deleteAllPrioritizedSubtasks();
    }

    @Override
    public void deleteEpics() {
        for (Integer epicId : epics.keySet()) {
            history.remove(epicId);
        }
        for (Integer subtaskId : subtasks.keySet()) {
            history.remove(subtaskId);
        }
        epics.clear();
        subtasks.clear();
    }


    private void updateEpicStatus(int epicId) {

        Epic epic = epics.get(epicId);
        if (epic == null) {

            return;
        }
        List<Subtask> subtasksForEpic = getEpicSubtasks(epicId);
        if (subtasksForEpic.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        ArrayList<TaskStatus> statusArray = new ArrayList<>();
        for (Subtask subtask : subtasksForEpic) {
            TaskStatus k = subtask.getStatus();
            statusArray.add(k);
        }
        boolean hasNew = statusArray.contains(TaskStatus.NEW);
        boolean hasDone = statusArray.contains(TaskStatus.DONE);
        boolean hasInProgress = statusArray.contains(TaskStatus.IN_PROGRESS);
        if (hasNew && !hasDone && !hasInProgress) {
            epic.setStatus(TaskStatus.NEW);
        } else if (!hasNew && hasDone && !hasInProgress) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }


    }

    public LocalDateTime getOldestStart(Epic epic) {
        return epic.getEpicSubtasks().stream()
                .map(this::getSubtask)
                .map(Subtask::getStart)
                .min(Comparator.naturalOrder())
                .orElse(null);

    }

    public LocalDateTime getNewestFinish(Epic epic) {
        return epic.getEpicSubtasks().stream()
                .map(this::getSubtask)
                .map(Subtask::getFinish)
                .max(Comparator.naturalOrder())
                .orElse(null);

    }

    public void setTimeForEpic(Epic epic) {
        if (epic.getEpicSubtasks().size() >= 2) {
            epic.setStartTime(getOldestStart(epic));
            epic.setFinishTime(getNewestFinish(epic));
            if (getOldestStart(epic) != null && getNewestFinish(epic) != null) {
                epic.setDuration(Duration.between(getOldestStart(epic), getNewestFinish(epic)));
            }
        } else {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setFinishTime(null);
        }
    }


}
