package manager;

import models.*;
import java.io.*;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (Writer writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения файла", e);
        }
    }

    private String toString(Task task) {
        String base = String.format("%d,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription()
        );

        if (task instanceof Subtask) {
            return base + "," + ((Subtask) task).getEpicId();
        } else {
            return base + ",";
        }
    }

    private Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        return switch (type) {
            case TASK -> new Task(id, name, description, status);
            case EPIC -> new Epic(id, name, description, status);
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[5]);
                yield new Subtask(id, name, description, status, epicId);
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        };
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
}
