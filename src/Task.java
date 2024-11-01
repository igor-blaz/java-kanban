
public class Task {

    private String name;
    private String description;
    private TaskStatus status;
    private int id;

    public Task(String name, String description, TaskStatus status){
        this.name = name;
        this.description = description;
        this.status = status;

    }



    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public void setStatus(TaskStatus status) {
        this.status = status;
    }


}
