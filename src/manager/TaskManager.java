package manager;

import models.Epic;
import models.Subtask;
import models.Task;
import java.util.List;

public interface TaskManager {

    int createTask(Task task);

    void updateTask(Task task);

    void deleteTask(int id);

    Task getTask(int id);

    List<Task> getAllTasks();

    void deleteAllTasks();

    int createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(int id);

    Subtask getSubtask(int id);

    List<Subtask> getAllSubtasks();

    void deleteAllSubtasks();

    int createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpic(int id);

    Epic getEpic(int id);

    List<Epic> getAllEpics();

    void deleteAllEpics();

    List<Subtask> getSubtasksByEpicId(int epicId);

    List<Task> getHistory();
}
