package manager;

import models.Epic;
import models.Subtask;
import models.Task;
import models.Status;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistoryManager();
    private int nextId = 1;

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private int generateNextId() {
        return nextId++;
    }

    @Override
    public int createTask(Task task) {
        task.setId(generateNextId());
        Task copy = task.copy();
        tasks.put(copy.getId(), copy);
        return copy.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) return;
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task.copy());
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public Task getTask(int id) {
        Task stored = tasks.get(id);
        if (stored != null) {
            historyManager.add(stored);
            return stored.copy();
        }
        return null;
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> result = new ArrayList<>();
        for (Task t : tasks.values()) {
            result.add(t.copy());
        }
        return result;
    }

    @Override
    public void deleteAllTasks() {
        for (Integer id : new ArrayList<>(tasks.keySet())) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        subtask.setId(generateNextId());
        Subtask copy = subtask.copy();
        subtasks.put(copy.getId(), copy);
        Epic epic = epics.get(copy.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(copy.getId());
            updateEpicStatus(epic);
        }
        return copy.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) return;
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask.copy());
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask removed = subtasks.remove(id);
        historyManager.remove(id);
        if (removed != null) {
            Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask stored = subtasks.get(id);
        if (stored != null) {
            historyManager.add(stored);
            return stored.copy();
        }
        return null;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        List<Subtask> result = new ArrayList<>();
        for (Subtask s : subtasks.values()) {
            result.add(s.copy());
        }
        return result;
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : new ArrayList<>(subtasks.keySet())) {
            historyManager.remove(id);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            for (Integer sid : new ArrayList<>(epic.getSubtaskIds())) {
                epic.removeSubtaskId(sid);
            }
            updateEpicStatus(epic);
        }
    }

    @Override
    public int createEpic(Epic epic) {
        epic.setId(generateNextId());
        Epic copy = epic.copy();
        epics.put(copy.getId(), copy);
        return copy.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) return;
        Epic existing = epics.get(epic.getId());
        if (existing != null) {
            existing.setName(epic.getName());
            existing.setDescription(epic.getDescription());
            existing.setStatus(epic.getStatus());
            updateEpicStatus(existing);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        historyManager.remove(id);
        if (epic != null) {
            for (Integer subId : new ArrayList<>(epic.getSubtaskIds())) {
                subtasks.remove(subId);
                historyManager.remove(subId);
            }
        }
    }

    @Override
    public Epic getEpic(int id) {
        Epic stored = epics.get(id);
        if (stored != null) {
            historyManager.add(stored);
            return stored.copy();
        }
        return null;
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> result = new ArrayList<>();
        for (Epic e : epics.values()) {
            result.add(e.copy());
        }
        return result;
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : new ArrayList<>(epics.keySet())) {
            historyManager.remove(id);
        }
        for (Integer id : new ArrayList<>(subtasks.keySet())) {
            historyManager.remove(id);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList();
        }
        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask.copy());
            }
        }
        return result;
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }
}