import manager.FileBackedTaskManager;
import models.*;
import org.junit.jupiter.api.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    File file;
    FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws Exception {
        file = File.createTempFile("task", ".csv");
        manager = new FileBackedTaskManager(file);
    }
    //Сохранение и загрузка пустого файла
    @Test
    void saveAndLoadEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loaded.getAllTasks().isEmpty());
    }
    //Сохранение и загрузка нескольких задач
    @Test
    void saveAndLoadWithTasks() {
        Task task = new Task(0, "Task", "Desc", Status.NEW);
        Task task1 = new Task(1, "Task1", "Desc1", Status.NEW);

        manager.createTask(task);
        manager.createTask(task1);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(2, loaded.getAllTasks().size());
        assertEquals("Task", loaded.getTask(task.getId()).getName());
        assertEquals("Task1", loaded.getTask(task1.getId()).getName());
    }
}
