package manager;

import models.Epic;
import models.Subtask;
import models.Task;
import models.Status;
import java.util.*;

public class InMemoryTaskManager implements TaskManager{
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistoryManager();
    private int nextId = 1;

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public int createTask(Task task) {
        task.setId(generateNextId());
        Task taskCopy = task.copy();
        tasks.put(task.getId(), taskCopy);
        return taskCopy.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null){
            historyManager.add(task.copy());
        }
        return task;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        subtask.setId(generateNextId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
        }
        return subtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null){
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
    }

    @Override
    public int createEpic(Epic epic) {
        epic.setId(generateNextId());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null){
           historyManager.add(epic);
        }
        return  epic;
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
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
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
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

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) {
                continue;
            }
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

    private int generateNextId() {
        return nextId++;
    }
}
