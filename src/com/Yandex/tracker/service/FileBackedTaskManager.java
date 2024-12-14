package com.Yandex.tracker.service;

import com.Yandex.tracker.model.*;

import java.io.*;
import java.nio.*;
import java.nio.file.Path;

public class FileBackedTaskManager extends InMemoryTaskManager {
    Path file;

    public FileBackedTaskManager(Path file) {
        this.file = file;
    }
    public void save() {

    }


}
