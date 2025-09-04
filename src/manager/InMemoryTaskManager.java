package manager;

import models.*;
import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistoryManager();
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );
    private int nextId = 1;

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public boolean hasTimeOverlap(Task task) {
        if (task.getStartTime() == null) return false;

        return prioritizedTasks.stream()
                .filter(t -> t.getStartTime() != null)
                .anyMatch(t -> isOverlap(t, task));
    }

    @Override
    public int createTask(Task task) {
        if (hasTimeOverlap(task)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей", null);
        }

        task.setId(generateNextId());
        Task copy = task.copy();
        tasks.put(copy.getId(), copy);
        if (copy.getStartTime() != null) {
            prioritizedTasks.add(copy);
        }
        return copy.getId();
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) return;
        if (tasks.containsKey(task.getId())) {
            Task existing = tasks.get(task.getId());
            prioritizedTasks.remove(existing);

            if (hasTimeOverlap(task)) {
                prioritizedTasks.add(existing);
                throw new ManagerSaveException("Задача пересекается по времени с существующей", null);
            }

            tasks.put(task.getId(), task.copy());
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task.copy());
            }
        }
    }

    @Override
    public void deleteTask(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            prioritizedTasks.remove(removed);
            historyManager.remove(id);
        }
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
            Task task = tasks.get(id);
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (hasTimeOverlap(subtask)) {
            throw new ManagerSaveException("Подзадача пересекается по времени с существующей", null);
        }

        subtask.setId(generateNextId());
        Subtask copy = subtask.copy();
        subtasks.put(copy.getId(), copy);
        if (copy.getStartTime() != null) {
            prioritizedTasks.add(copy);
        }

        Epic epic = epics.get(copy.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(copy.getId());
            updateEpic(epic);
        }
        return copy.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) return;
        if (subtasks.containsKey(subtask.getId())) {
            Subtask existing = subtasks.get(subtask.getId());
            prioritizedTasks.remove(existing);

            if (hasTimeOverlap(subtask)) {
                prioritizedTasks.add(existing);
                throw new ManagerSaveException("Подзадача пересекается по времени с существующей", null);
            }

            subtasks.put(subtask.getId(), subtask.copy());
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask.copy());
            }

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpic(epic);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask removed = subtasks.remove(id);
        if (removed != null) {
            prioritizedTasks.remove(removed);
            historyManager.remove(id);

            Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpic(epic);
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
        return subtasks.values().stream()
                .map(Subtask::copy)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : new ArrayList<>(subtasks.keySet())) {
            Subtask subtask = subtasks.get(id);
            prioritizedTasks.remove(subtask);
            historyManager.remove(id);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            for (Integer sid : new ArrayList<>(epic.getSubtaskIds())) {
                epic.removeSubtaskId(sid);
            }
            updateEpic(epic);
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
            updateEpicStatusAndTime(existing);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        historyManager.remove(id);
        if (epic != null) {
            for (Integer subId : new ArrayList<>(epic.getSubtaskIds())) {
                Subtask subtask = subtasks.remove(subId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                    historyManager.remove(subId);
                }
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
        return epics.values().stream()
                .map(Epic::copy)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllEpics() {
        for (Integer id : new ArrayList<>(epics.keySet())) {
            historyManager.remove(id);
        }
        for (Integer id : new ArrayList<>(subtasks.keySet())) {
            Subtask subtask = subtasks.get(id);
            prioritizedTasks.remove(subtask);
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

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Subtask::copy)
                .collect(Collectors.toList());
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

    private void updateEpicStatusAndTime(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;
        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }

            if (subtask.getStartTime() != null) {
                if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subtask.getStartTime();
                }

                LocalDateTime subtaskEnd = subtask.getEndTime();
                if (latestEnd == null || subtaskEnd.isAfter(latestEnd)) {
                    latestEnd = subtaskEnd;
                }
            }

            totalDuration = totalDuration.plus(subtask.getDuration());
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }

        epic.setStartTime(earliestStart);
        epic.setDuration(totalDuration);
        epic.setEndTime(latestEnd);
    }

    private boolean isOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) return false;

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private int generateNextId() {
        return nextId++;
    }
}