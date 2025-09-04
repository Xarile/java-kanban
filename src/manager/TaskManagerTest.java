package manager;

import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createManager();

    @BeforeEach
    void init() {
        taskManager = createManager();
    }
    // Проверить корректность создания и получения задачи из менеджера
    @Test
    void testCreateAndGetTask() {
        Task task = new Task(0, "Task1", "Desc1", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now());
        int id = taskManager.createTask(task);

        Task saved = taskManager.getTask(id);
        assertNotNull(saved, "Задача должна сохраняться");
        assertEquals("Task1", saved.getName(), "Имя должно совпадать");
    }
    // Проверить связь между эпиком и его подзадачами
    @Test
    void testCreateAndGetEpicWithSubtasks() {
        Epic epic = new Epic(0, "Epic1", "Desc", Status.NEW);
        int epicId = taskManager.createEpic(epic);

        Subtask sub = new Subtask(0, "Sub1", "SubDesc", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        int subId = taskManager.createSubtask(sub);

        Epic savedEpic = taskManager.getEpic(epicId);
        assertTrue(savedEpic.getSubtaskIds().contains(subId), "Эпик должен содержать подзадачу");
    }
    // Проверить корректность удаления задач из менеджера
    @Test
    void testDeleteTask() {
        Task task = new Task(0, "Task2", "Desc2", Status.NEW);
        int id = taskManager.createTask(task);
        taskManager.deleteTask(id);

        assertNull(taskManager.getTask(id), "Удалённая задача должна отсутствовать");
    }
    // Проверить расчет статуса эпика, когда все подзадачи имеют статус NEW
    @Test
    void testEpicStatusWhenAllSubtasksNew() {
        Epic epic = new Epic(0, "EpicTest", "Desc", Status.NEW);
        int epicId = taskManager.createEpic(epic);

        taskManager.createSubtask(new Subtask(0, "Sub1", "S1", Status.NEW, epicId));
        taskManager.createSubtask(new Subtask(0, "Sub2", "S2", Status.NEW, epicId));

        Epic saved = taskManager.getEpic(epicId);
        assertEquals(Status.NEW, saved.getStatus(), "Если все NEW — эпик тоже NEW");
    }
    // Проверить расчет статуса эпика, когда все подзадачи имеют статус DONE
    @Test
    void testEpicStatusWhenAllSubtasksDone() {
        Epic epic = new Epic(0, "EpicTest", "Desc", Status.NEW);
        int epicId = taskManager.createEpic(epic);

        taskManager.createSubtask(new Subtask(0, "Sub1", "S1", Status.DONE, epicId));
        taskManager.createSubtask(new Subtask(0, "Sub2", "S2", Status.DONE, epicId));

        Epic saved = taskManager.getEpic(epicId);
        assertEquals(Status.DONE, saved.getStatus(), "Если все DONE — эпик DONE");
    }
    // Проверить расчет статуса эпика при смешанных статусах подзадач
    @Test
    void testEpicStatusWhenMixedNewAndDone() {
        Epic epic = new Epic(0, "EpicTest", "Desc", Status.NEW);
        int epicId = taskManager.createEpic(epic);

        taskManager.createSubtask(new Subtask(0, "Sub1", "S1", Status.NEW, epicId));
        taskManager.createSubtask(new Subtask(0, "Sub2", "S2", Status.DONE, epicId));

        Epic saved = taskManager.getEpic(epicId);
        assertEquals(Status.IN_PROGRESS, saved.getStatus(), "Смешанные NEW/DONE = IN_PROGRESS");
    }
    // Проверить расчет статуса эпика при наличии подзадач IN_PROGRESS
    @Test
    void testEpicStatusWhenInProgressSubtasks() {
        Epic epic = new Epic(0, "EpicTest", "Desc", Status.NEW);
        int epicId = taskManager.createEpic(epic);

        taskManager.createSubtask(new Subtask(0, "Sub1", "S1", Status.IN_PROGRESS, epicId));

        Epic saved = taskManager.getEpic(epicId);
        assertEquals(Status.IN_PROGRESS, saved.getStatus(), "Если есть IN_PROGRESS — эпик тоже IN_PROGRESS");
    }
    // Проверить пересечения задач
    @Test
    void testTimeOverlapPrevention() {
        Task t1 = new Task(0, "Task1", "Desc", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 9, 9, 10, 0));
        taskManager.createTask(t1);

        Task t2 = new Task(0, "Task2", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 9, 9, 10, 30));

        assertThrows(RuntimeException.class, () -> taskManager.createTask(t2),
                "Пересекающиеся задачи должны выдавать исключение");
    }
}
