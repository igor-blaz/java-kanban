package com.Yandex.tracker.service;
import java.util.ArrayList;
import com.Yandex.tracker.model.*;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int id;
    private final HistoryManager history = Managers.getDefaultHistory();

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
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> SubtaskArray = new ArrayList<>();
        if (epics.containsKey(epicId)) {
            Epic epic = epics.get(epicId);
            ArrayList<Integer> SubtasksForEpic = epic.getSubtasksForEpic();
            for (Integer subtaskId : SubtasksForEpic) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    SubtaskArray.add(subtask);
                }
            }
        }
        return SubtaskArray;
    }
    @Override
    public Task getTask(int id) {
        history.add(tasks.get(id));//???
        Task task = tasks.get(id);
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

        task.setId(id++);
        task.setStatus(TaskStatus.NEW);
        tasks.put(id, task);
        return id;
    }
    @Override
    public int addNewEpic(Epic epic) {
        epic.setId(id++);
        epics.put(id, epic);
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        final int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        final int id = ++this.id;
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtasksForEpic(subtask.getId());
        updateEpicStatus(epicId);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
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
        }
    }
    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }
    @Override
    public void deleteEpic(int id) {
        epics.remove(id);
        final Epic epic = epics.remove(id);
        for (Integer subtaskId : epic.getSubtasksForEpic()) {
            subtasks.remove(subtaskId);
        }
    }
    @Override
    public void deleteSubtask(int id) {        //получили id subtaskA
        Subtask subtask = subtasks.remove(id);
        Epic epic = epics.get(subtask.getEpicId());

        ArrayList<Integer> subtasksIdForEpics = epic.getSubtasksForEpic();
        for (int i = 0; i < subtasksIdForEpics.size(); i++) {
            if(subtasksIdForEpics.get(i)==id){
                subtasksIdForEpics.remove(i);
                break;
            }
        }
        updateEpicStatus(epic.getId());
    }
    @Override
    public void deleteTasks() {
        tasks.clear();
    }
    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksForEpic().clear();
            updateEpicStatus(epic.getId());
        }
    }
    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }
    @Override
    public void updateEpicStatus(int EpicId) {

        Epic epic = epics.get(EpicId);
        if (epic == null) {

            return;
        }
        ArrayList<Subtask> SubtasksForEpic = getEpicSubtasks(EpicId);
        if (SubtasksForEpic.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        ArrayList<TaskStatus> statusArray = new ArrayList<>();
        for (int i = 0; i < SubtasksForEpic.size(); i++) {
            Subtask subtask = SubtasksForEpic.get(i);
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
}
