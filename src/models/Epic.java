package models;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return List.copyOf(subtaskIds);
    }

    public void addSubtaskId(int subtaskId) {
        if (subtaskId == getId()) {
            throw new IllegalArgumentException("Эпик не может быть подзадачей самого себя");
        }
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public String toString() {
        return "Epic {" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }

    @Override
    public Epic copy() {
        Epic copy = new Epic(getId(), getName(), getDescription(), getStatus());
        for (Integer subtaskId : subtaskIds) {
            copy.addSubtaskId(subtaskId);
        }
        return copy;

    }
}