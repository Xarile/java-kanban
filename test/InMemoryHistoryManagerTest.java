import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import models.Task;
import models.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void testDuplicatePrevention() {
        Task task = new Task(1, "Task", "Desc", Status.NEW);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Дубликатов быть не должно");
    }

    @Test
    void testRemoveFromHistory() {
        Task t1 = new Task(1, "T1", "D1", Status.NEW);
        Task t2 = new Task(2, "T2", "D2", Status.NEW);
        Task t3 = new Task(3, "T3", "D3", Status.NEW);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(1); // удаление начала
        historyManager.remove(2); // середина
        historyManager.remove(3); // конец

        assertTrue(historyManager.getHistory().isEmpty(), "После удаления всё должно быть пусто");
    }
}