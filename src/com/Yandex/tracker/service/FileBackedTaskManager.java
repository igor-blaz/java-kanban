package com.Yandex.tracker.service;

import com.Yandex.tracker.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    final private File file;
    final static int ID_POSITION = 0;
    final static int TYPE_POSITION = 1;
    final static int NAME_POSITION = 2;
    final static int STATUS_POSITION = 3;
    final static int DESCRIPTION_POSITION = 4;
    final static int EPIC_ID_POSITION = 5;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        List<String> lineTask = new ArrayList<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            while ((line = reader.readLine()) != null) {
                lineTask.add(line);
            }
            lineTask.remove(0);
            for (String str : lineTask) {
                Task task = manager.fromString(str);
                if (task.getClass() == Task.class) {
                    manager.addNewTask(task);
                } else if (task.getClass() == Subtask.class) {
                    Subtask subtask = (Subtask) task;
                    manager.addNewSubtask(subtask);
                } else if (task.getClass() == Epic.class) {
                    Epic epic = (Epic) task;
                    manager.addNewEpic(epic);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла.");
        }
        return manager;

    }


    public void save() {


        try (FileWriter writer = new FileWriter(file)) {

            writer.write("id,type,name,status,description,epic\n");
            if (super.getTasks() != null) {
                for (Task task : super.getTasks()) {
                    writer.write(toString(task));
                    writer.write("\n");
                }
            }
            if (super.getSubtasks() != null) {
                for (Subtask subtask : super.getSubtasks()) {
                    writer.write(toString(subtask));
                    writer.write("\n");
                }
            }
            if (super.getEpics() != null) {
                for (Epic epic : super.getEpics()) {
                    writer.write(toString(epic));
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время записи в файл.");

        }
    }

    String toString(Task task) {

        List<String> taskInfo = new ArrayList<>(5);
        TaskType taskType;


        taskInfo.add(ID_POSITION, String.valueOf(task.getId()));
        taskInfo.add(TYPE_POSITION, null);
        taskInfo.add(NAME_POSITION, String.valueOf(task.getName()));
        taskInfo.add(STATUS_POSITION, String.valueOf(task.getStatus()));
        taskInfo.add(DESCRIPTION_POSITION, String.valueOf(task.getDescription()));


        if (task.getClass() == Subtask.class) {
            taskType = TaskType.SUBTASK;
            taskInfo.set(TYPE_POSITION, String.valueOf(taskType));
            taskInfo.add(EPIC_ID_POSITION, String.valueOf(((Subtask) task).getEpicId()));

        } else if (task.getClass() == Task.class) {
            taskType = TaskType.TASK;
            taskInfo.set(TYPE_POSITION, String.valueOf(taskType));
        } else if (task.getClass() == Epic.class) {
            taskType = TaskType.EPIC;
            taskInfo.set(TYPE_POSITION, String.valueOf(taskType));
        } else if (taskInfo.get(TYPE_POSITION) == null) {
            System.out.println("Невозможно определить тип задачи.");
            return null;
        } else {
            System.out.println("Невозможно создать строку");
            return null;
        }

        return taskInfo.toString().replace(" ", "").
                replace("]", "").replace("[", "");
    }

    Task fromString(String value) {


        String[] rawTask = value.split(",");
        String unsureId = rawTask[ID_POSITION];
        String type = rawTask[TYPE_POSITION];
        String name = rawTask[NAME_POSITION];
        String unsureStatus = rawTask[STATUS_POSITION];
        String description = rawTask[DESCRIPTION_POSITION];

        if (isInteger(unsureId) && isCorrectStatus(unsureStatus) && isCorrectType(type)) {
            int id = Integer.parseInt(rawTask[ID_POSITION]);
            TaskStatus status = statusConverter(unsureStatus);

            if (type.equals("TASK")) {
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            } else if (type.equals("SUBTASK") && rawTask.length == 5) {
                if (isInteger(rawTask[EPIC_ID_POSITION])) {
                    int epicId = Integer.parseInt(rawTask[EPIC_ID_POSITION]);
                    Subtask subtask = new Subtask(name, description, status, epicId);
                    subtask.setId(id);
                    return subtask;
                }

            } else if (type.equals("EPIC")) {
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                return epic;

            }

        }
        return null;
    }

    public TaskStatus statusConverter(String value) {
        TaskStatus status;

        if (value.equals("NEW")) {
            status = TaskStatus.NEW;
        } else if (value.equals("DONE")) {
            status = TaskStatus.DONE;
        } else if (value.equals("IN_PROGRESS")) {
            status = TaskStatus.IN_PROGRESS;
        } else {
            System.out.println("Невозможно определить статус задачи " + value);
            return null;
        }


        return status;
    }

    public boolean isCorrectStatus(String value) {
        return (value.equals("NEW") || value.equals("DONE") || value.equals("IN_PROGRESS"));
    }

    public boolean isCorrectType(String value) {
        return (value.equals("TASK") || value.equals("SUBTASK") || value.equals("EPIC"));
    }

    public boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Ошибка форматирования");
            return false;
        }

    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    public File getFile() {
        return file;
    }


}
