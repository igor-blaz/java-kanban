package com.Yandex.tracker.service;

import com.Yandex.tracker.model.Epic;
import com.Yandex.tracker.model.Subtask;
import com.Yandex.tracker.model.Task;

import java.util.Collections;
import java.util.ArrayList;

import com.Yandex.tracker.model.Subtask;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node<Task>> history = new HashMap<>();
    private Node head;
    private Node tail;
    List<Task> historyArray = new ArrayList<>();

    @Override
    public List<Task> getHistory() {
        Collections.reverse(historyArray);
        return historyArray;
    }

    public Task makeCopy(Task task) {
        Task clone = new Task(task.getName(), task.getDescription(), task.getStatus());
        clone.setId(task.getId());
        return clone;
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            Task clone = makeCopy(task);
            if (history.containsKey(clone.getId())) {
                remove(clone.getId());
                historyArray.remove(task);
            }
            Node newNode = new Node(clone);
            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                tail.next = newNode;
                newNode.prev = tail;
                tail = newNode;
            }
            history.put(clone.getId(), newNode);
            historyArray.add(clone);
        }
    }

    @Override
    public void remove(int id) {
        Node currentNode = history.get(id);
        removeNode(currentNode);


    }

    void removeNode(Node currentNode) {
        if (currentNode == null) {
            return;
        }
        if (currentNode == head && currentNode != tail) {
            Node nextNode = currentNode.next;   //определили следующий элемент
            nextNode.prev = null;                // удалили у него начало тк это HEAD
            head = nextNode;
            //записали в поле новое значение
        } else if (currentNode == tail && currentNode != head) {
            Node prevNode = currentNode.prev;
            prevNode.next = null;
            tail = prevNode;
        } else if (currentNode == head && currentNode == tail) {

            head = null;
            tail = null;

        } else {
            Node nextNode = currentNode.next;
            Node prevNode = currentNode.prev;
            prevNode.next = currentNode.next;
            nextNode.prev = currentNode.prev;
        }


    }

}
