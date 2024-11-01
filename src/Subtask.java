
public class Subtask extends Task {
    public int epicId;
    private TaskStatus status;
    private int id;


    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;

    }

    public void setId(int id) {
        this.id = id;
    }

    // Геттер и сеттер для поля epicId
    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public int getId(Subtask subtask) {
        return id;
    }

    public TaskStatus getStatus(Subtask subtask) {
        return subtask.status;
    }


}

