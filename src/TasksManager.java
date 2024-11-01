import java.util.HashMap;
import java.util.ArrayList;

public class TasksManager {
    //здесь хранятся все таски
    //TasksManager manager = new TasksManager();
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private int id = 1;

// Получить все задачи
    //тут мне надо вернуть массив типа task

    public ArrayList<Task> getTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        for (Task value : tasks.values()) {
            allTasks.add(value);

        }
        return allTasks;
    }

    public ArrayList<Subtask> getSubtasks() {
        ArrayList<Subtask> allSubTasks = new ArrayList<>();
        for (Subtask value : subtasks.values()) {
            allSubTasks.add(value);

        }
        return allSubTasks;
    }

    public ArrayList<Epic> getEpics() {
        ArrayList<Epic> allEpics = new ArrayList<>();
        for (Epic value : epics.values()) {
            allEpics.add(value);
        }
        return allEpics;
    }

    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> SubtaskArray = new ArrayList<>();
        if (epics.containsKey(epicId)) {
            Epic epic = epics.get(epicId);
            ArrayList<Integer> SubtasksForEpic = epic.getSubtasksForEpic(); //массив id subtask, но не сами subtask
            for (Integer subtaskId : SubtasksForEpic) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) { // Проверка на null, если subtask с таким id не найден
                    SubtaskArray.add(subtask);
                }
            }
        }
        return SubtaskArray;
    }


    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

//Добавляем задачи

    public int addNewTask(Task task) {

        task.setId(id++);
        task.setStatus(TaskStatus.NEW);
        tasks.put(id, task);
        return id;
    }


    public int addNewEpic(Epic epic) {
        epic.setId(id++);
        epics.put(id, epic);
        checkEpicStatus(id);
        return id;
    }
//Возвращается номер элемента в массиве в SubtasksForEpic

    public Integer addNewSubtask(Subtask subtask) {
        subtask.setId(id++);
        subtasks.put(id, subtask);
        int EpicId = subtask.getEpicId();

        if (epics.containsKey(EpicId)) {
            Epic epic = epics.get(EpicId);
            epic.addSubtasksForEpic(id);

        }

        checkEpicStatus(EpicId);
        return id;
    }


    // МЕНЯЕМ СТАТУС
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }

    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }

        checkEpicStatus(epic.getId());
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
        }
        checkEpicStatus(subtask.getEpicId());
    }


    //УДАЛЯЕМ ЗАДАЧИ
    void deleteTask(int id) {
        tasks.remove(id);
    }

    void deleteEpic(int id) {
        epics.remove(id);
        this.killSubtask(id);

    }

    void deleteSubtask(int id) {
        subtasks.remove(id);
        Subtask subtask = subtasks.get(id);
        this.actualizeEpicArray(subtask);
        checkEpicStatus(subtask.getEpicId());
    }


    void deleteTasks() {
        tasks.clear();
    }

    void deleteSubtasks() {
        subtasks.clear();

    }

    void deleteEpics() {
        epics.clear();
        subtasks.clear();  //Я как понял, subtasks не могут существовать без Эпиков
    }

    //id Subtask
    public void actualizeEpicArray(Subtask subtask) {        //удаляем id subtask в массиве у epic
        int SubtaskId = subtask.getId();         //id
        int EpicId = subtask.getEpicId();      //id эпика у субтаска
        if (epics.containsKey(EpicId)) {          //Нашли эпик
            Epic epic = epics.get(EpicId);       //Обозначили эпик
            ArrayList<Integer> SubtasksForEpic = epic.getSubtasksForEpic();  //Нашли его массив

            if (SubtasksForEpic.contains(SubtaskId)) {       //Если есть старая версия
                SubtasksForEpic.remove(SubtaskId);            // удалить старую версию

            }
        }

    }

    public void killSubtask(int EpicId) {               //получаем id Эпика
        Epic epic = epics.get(EpicId);
        if(epic==null){
            return;
        }
        ArrayList<Integer> SubtasksForEpic = epic.getSubtasksForEpic(); //нашли id subtask на удаление
        for (int i = 0; i < SubtasksForEpic.size(); i++) {
            int idForDelete = SubtasksForEpic.get(i);               //id subtask на удаление
            if (subtasks.containsKey(idForDelete)) {
                subtasks.remove(idForDelete);
            }
        }

    }

    public void checkEpicStatus(int EpicId) {

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
            TaskStatus k = subtask.getStatus(subtask);
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
