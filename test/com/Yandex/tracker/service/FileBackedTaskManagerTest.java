package com.Yandex.tracker.service;

import com.Yandex.tracker.model.Epic;
import com.Yandex.tracker.model.Subtask;
import com.Yandex.tracker.model.Task;
import com.Yandex.tracker.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;


import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private File file;
    private Task taskOne;
    private Epic taskTwo;
    private Subtask taskThree;
    private FileBackedTaskManager fileBacked;
    private String stringedTask;

    @BeforeEach
    void setUp() {

        fileBacked = new FileBackedTaskManager(file);

        taskOne = new Task("имя", "описание", TaskStatus.NEW);
        taskOne.setId(17);
        taskTwo = new Epic("имя2", "описание2", TaskStatus.NEW);
        taskTwo.setId(99);
        taskThree = new Subtask("имя2", "описание2", TaskStatus.NEW, 99);
        taskThree.setId(4);

    }

    @Test
    void toStringTest() {
        String staskOneString = fileBacked.toString(taskOne);
        String staskTwoString = fileBacked.toString(taskTwo);
        String staskThreeString = fileBacked.toString(taskThree);
        assertEquals(staskOneString,"17,TASK,имя,NEW,описание");
        assertEquals(staskTwoString,"19,EPIC,имя2,NEW,описание2");
        assertEquals(staskThreeString,"4,SUBTASK,имя2,NEW,описание2,99");
    }

    @Test
    void incorrectTaskToStringTest() {
        taskOne = new Task("имя", "описание", TaskStatus.NEW);
        String taskOneString = fileBacked.toString(taskOne);
        // System.out.println(taskOneString);
        assertEquals(taskOneString, ("0,TASK,имя,NEW,описание"));
    }

    @Test
    void fromStringTest() {
        stringedTask = ("100,EPIC,купить_хлеб,IN_PROGRESS,Сходить_в_Ленту");
        Task task = fileBacked.fromString(stringedTask);
        assertEquals(task.getId(), 100);
        assertEquals(task.getName(), ("купить_хлеб"));
        assertEquals(task.getStatus().toString(), ("IN_PROGRESS"));
        assertEquals(task.getDescription(), ("Сходить_в_Ленту"));
    }

    @Test
    void incorrectTaskFromStringTest() {
        String incorrect = ("сто,EPI,купить хлеб,IN_PROGRESS,3456345");
        String incorrectStatus = ("100,EPIC,купить_хлеб,ERROR,Сходить_в_Ленту");
        String incorrectType = ("100,СЛОЖНО!!!,купить_хлеб,DONE,Сходить_в_Ленту");
        Task taskFailOne = fileBacked.fromString(incorrect);
        Task taskFailTwo = fileBacked.fromString(incorrectStatus);
        Task taskFailThree = fileBacked.fromString(incorrectType);
        assertNull(taskFailOne);
        assertNull(taskFailTwo);
        assertNull(taskFailThree);
        System.out.println("ошибка предусмотрена тестом incorrectTaskFromStringTest");
    }

    @Test
    void saveTest() {
        String fromFile;
        try {
            File tempFile = File.createTempFile("temp", ".csv");
            FileBackedTaskManager newFileBacked = new FileBackedTaskManager(tempFile);
            newFileBacked.addNewTask(taskOne);
            newFileBacked.save();
            File fromFileBacked = newFileBacked.getFile();

            String expectedString = "id,type,name,status,description,epic\n" +
                    "0,TASK,имя,NEW,описание";
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(fromFileBacked))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }

            } catch (IOException e) {
                fail("Ошибка при чтении файла: " + e.getMessage());
            }
            fromFile = sb.toString();
            //System.out.println(fromFile);
            //System.out.println(expectedString);`

            assertEquals(fromFile.replace("\n", ""),
                    expectedString.replace("\n", ""));
        } catch (IOException e) {
            System.out.println("Не удалось создать временный файл  " + e.getMessage());
        }
    }

    @Test
    void testLoadFromFile() throws IOException {

        File tempFile = File.createTempFile("temp", ".txt");
        FileBackedTaskManager newFileBacked = new FileBackedTaskManager(tempFile);
        newFileBacked.addNewTask(taskOne);
        newFileBacked.addNewSubtask(taskThree);
        newFileBacked.addNewEpic(taskTwo);
        newFileBacked.save();
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, manager.getTasks().size(), "Должна быть только одна задача");
        assertEquals(1, manager.getEpics().size(), "Должен быть только один эпик");
        assertEquals(1, manager.getSubtasks().size(), "Должна быть только одна подзадача");
    }
}


