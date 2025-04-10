package test;

import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private TaskManager manager;
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        historyManager = Managers.getDefaultHistoryManager();
    }

    // 1. Проверка равенства Task по id
    @Test
    void tasksEqualId() {
        Task task1 = new Task(1, "Task 1", "Description", Status.NEW);
        Task task2 = new Task(1, "Task 2", "Different", Status.DONE);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    // 2. Проверка равенства наследников Task по id
    @Test
    void subtaskAndEpicEqualById() {
        Task subtask = new Subtask(1, "Sub", "Desc", Status.NEW, 0);
        Task epic = new Epic(1, "Epic", "Desc",Status.NEW);

        assertNotEquals(subtask, epic, "Разные типы с одинаковым id не должны быть равны");
    }

    // 3. Epic не может быть подзадачей самого себя
    @Test
    void epicCantBeItsOwnSubtask() {
        Epic epic = new Epic(1, "Epic", "Desc", Status.NEW);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtaskId(epic.getId());
        });

        assertEquals("Эпик не может быть подзадачей самого себя", exception.getMessage());

    }

    // 4. Subtask не может быть своим эпиком
    @Test
    void subtaskCantBeItsOwnEpic() {
        Subtask subtask = new Subtask(1, "Subtask", "Description", Status.NEW, 1);

        // Попытка установить эпик подзадаче как саму себя
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            subtask.setEpicId(subtask.getId());
        });

        assertEquals("Подзадача не может быть своим собственным эпиком", exception.getMessage());

    }

    // 5. Проверка утилитарного класса
    @Test
    void managersUtilityClassCheck() {
        assertNotNull(manager, "Менеджер должен быть проинициализирован");
        assertNotNull(historyManager, "Менеджер истории должен быть проинициализирован");

        // Проверка готовности к работе
        Task task = new Task(1, "Test", "Desc", Status.NEW);
        int id = manager.createTask(task);
        assertNotNull(manager.getTask(id), "Менеджер должен сохранять задачи");
    }

    // 6. Проверка добавления разных типов задач
    @Test
    void managerShouldStoreAllTaskTypes() {
        Task task = new Task(1,"Task", "Desc", Status.NEW);
        Epic epic = new Epic(1,"Epic", "Desc", Status.DONE);
        Subtask subtask = new Subtask(1,"Sub", "Desc", Status.NEW, epic.getId());

        int taskId = manager.createTask(task);
        int epicId = manager.createEpic(epic);
        int subtaskId = manager.createSubtask(subtask);

        assertAll(
                () -> assertNotNull(manager.getTask(taskId)),
                () -> assertNotNull(manager.getEpic(epicId)),
                () -> assertNotNull(manager.getSubtask(subtaskId))
        );
    }

    // 7. Проверка конфликта id
    @Test
    void checkingForIdConflict() {
        Task manualTask = new Task(999, "Manual", "Desc", Status.NEW);
        manager.createTask(manualTask);

        Task autoTask = new Task(1,"Auto", "Desc", Status.NEW);
        int autoId = manager.createTask(autoTask);

        assertNotEquals(999, autoId, "ID должны быть уникальными");
    }

    // 8. Проверка неизменности задачи
    @Test
    void taskShouldBeImmutableWhenAdded() {
        Task original = new Task(1, "Original", "Desc", Status.NEW);
        int id = manager.createTask(original);

        original.setName("Modified");
        original.setStatus(Status.DONE);

        Task stored = manager.getTask(id);
        assertAll(
                () -> assertEquals("Original", stored.getName()),
                () -> assertEquals(Status.NEW, stored.getStatus())
        );
    }

    // 9. История должна сохранять версии задач
    @Test
    void historyShouldKeepTaskVersions() {
        Task task = new Task(1, "Task v1", "Desc", Status.NEW);
        Task task1 = new Task(1,"Task v2" ,"Desc", Status.NEW );

        historyManager.add(task);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals("Task v1", history.get(0).getName(), "Должна сохраняться первая версия");
        assertEquals("Task v2", history.get(1).getName(), "Должна сохраняться вторая версия");
    }
    // Пример теста из подсказки
    @Test
    void add() {
        Task task = new Task(1,"Task", "Desc",Status.NEW);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }
}