package manager;

import models.*;
import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) {
                if (!lines[i].isBlank()) {
                    Task task = manager.fromString(lines[i]);
                    manager.addWithoutSave(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки файла", e);
        }
        return manager;
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    private void save() {
        try (Writer writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,duration,startTime,epic\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения файла", e);
        }
    }

    private String toString(Task task) {
        String durationStr = task.getDuration() != null ?
                String.valueOf(task.getDuration().toMinutes()) : "0";
        String startTimeStr = task.getStartTime() != null ?
                task.getStartTime().toString() : "null";

        String base = String.format("%d,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                durationStr,
                startTimeStr);

        if (task instanceof Subtask) {
            return base + "," + ((Subtask) task).getEpicId();
        } else {
            return base;
        }
    }

    private Task fromString(String line) {
        line = line.replaceAll("[\\[\\]\"]", "").trim();
        String[] fields = line.split(",");

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        try {
            int id = Integer.parseInt(fields[0]);
            TaskType type = TaskType.valueOf(fields[1]);
            String name = fields[2];
            Status status = Status.valueOf(fields[3]);
            String description = fields[4];

            Duration duration = Duration.ofMinutes(Long.parseLong(fields[5]));
            LocalDateTime startTime = "null".equals(fields[6]) ? null : LocalDateTime.parse(fields[6]);

            switch (type) {
                case TASK:
                    return new Task(id, name, description, status, duration, startTime);
                case EPIC:
                    Epic epic = new Epic(id, name, description, status);
                    epic.setDuration(duration);
                    epic.setStartTime(startTime);
                    return epic;
                case SUBTASK:
                    int epicId = Integer.parseInt(fields[7]);
                    return new Subtask(id, name, description, status, epicId, duration, startTime);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга строки: " + line);
            e.printStackTrace();
            throw new ManagerSaveException("Ошибка парсинга задачи из строки: " + line, e);
        }
    }

    private void addWithoutSave(Task task) {
        switch (task.getType()) {
            case TASK:
                super.createTask(task);
                break;
            case EPIC:
                super.createEpic((Epic) task);
                break;
            case SUBTASK:
                super.createSubtask((Subtask) task);
                break;
        }
    }
}
