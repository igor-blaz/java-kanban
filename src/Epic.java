
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> SubtasksForEpic = new ArrayList<>();
    private int id;
    private TaskStatus status;

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
    }

    public ArrayList<Integer> getSubtasksForEpic() {
        return SubtasksForEpic;
    }

    public void setSubtasksForEpic(ArrayList<Integer> SubtasksForEpic) {
        this.SubtasksForEpic = SubtasksForEpic;
    }

    public void addSubtasksForEpic(int id) {
        SubtasksForEpic.add(id);
    }
    public int getId(){
        return id;
    }



}

