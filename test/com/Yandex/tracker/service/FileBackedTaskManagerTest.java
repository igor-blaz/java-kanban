package com.Yandex.tracker.service;

import com.Yandex.tracker.model.Epic;
import com.Yandex.tracker.model.Subtask;
import com.Yandex.tracker.model.Task;
import com.Yandex.tracker.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;


import static com.Yandex.tracker.service.FileBackedTaskManager.loadFromFile;
import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private File file;
    private Task taskOne;
    private Epic taskTwo;
    private Subtask taskThree;
    private FileBackedTaskManager fileBacked;

    @BeforeEach
    void setUp() {

        fileBacked = new FileBackedTaskManager(file);

        taskOne = new Task("имя", "описание", TaskStatus.NEW);
        taskOne.setId(17);
        taskTwo = new Epic("имя2", "описание2", TaskStatus.NEW);
        taskTwo.setId(99);
        taskThree = new Subtask("имя2", "описание2", TaskStatus.NEW, taskTwo.getId());
        taskThree.setId(4);

    }


    @Test
    void incorrectTaskFromStringTest() {

        String incorrect = ("37,EPI,купить хлеб,IN_PROGRESS,3456345");
        String incorrectStatus = ("1000,EPIC,купить_хлеб,ERROR,Сходить_в_Ленту");
        String incorrectType = ("100,СЛОЖНО!!!,купить_хлеб,DONE,Сходить_в_Ленту");


        File incorrectFile = new File(incorrect);
        File incorrectStatusFile = new File(incorrectStatus);
        File incorrectTypeFile = new File(incorrectType);

        FileBackedTaskManager incorrectFromManager = loadFromFile(incorrectFile);
        FileBackedTaskManager incorrectStatusFromManager = loadFromFile(incorrectStatusFile);
        FileBackedTaskManager incorrectTypeFromManager = loadFromFile(incorrectTypeFile);

        Task incorrectOne = incorrectFromManager.getTask(37);
        Task incorrectTwo = incorrectStatusFromManager.getTask(1000);
        Task incorrectThree = incorrectTypeFromManager.getTask(100);

        assertNull(incorrectOne);
        assertNull(incorrectTwo);
        assertNull(incorrectThree);

    }
}


