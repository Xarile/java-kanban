package manager;

import java.util.*;
import models.Task;
import models.Epic;
import models.Subtask;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
        Task taskCopy = task.copy();

        history.add(taskCopy);
    }

    @Override
    public List<Task> getHistory(){
        return new ArrayList<>(history);
    }
}