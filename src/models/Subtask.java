package models;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    @Override
    public Subtask copy() {
        return new Subtask(getId(), getName(), getDescription(), getStatus(), getEpicId());
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId == getId()) {
            throw new IllegalArgumentException("Подзадача не может быть своим собственным эпиком");
        }
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask {" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }


}