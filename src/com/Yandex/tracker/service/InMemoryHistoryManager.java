package com.Yandex.tracker.service;


import com.Yandex.tracker.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node<Task>> history = new HashMap<>();
    private Node head;
    private Node tail;


    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node<Task> current = head;
        while (current != null) {
            tasks.add(current.data);
            current = current.next;
        }
        return tasks;
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            if (history.containsKey(task.getId())) {
                Node currentNode = history.get(task.getId());
                removeNode(currentNode);
            }
            Node newNode = new Node(task);
            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                tail.next = newNode;
                newNode.prev = tail;
                tail = newNode;
            }
            history.put(task.getId(), newNode);
        }
    }

    @Override
    public void remove(int id) {
        Node currentNode = history.remove(id);
        removeNode(currentNode);


    }

    void removeNode(Node currentNode) {
        if (currentNode == null) {
            return;
        }
        if (currentNode == head && currentNode != tail) {
            Node nextNode = currentNode.next;
            nextNode.prev = null;
            head = nextNode;

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
