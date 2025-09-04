import manager.FileBackedTaskManager;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;
import manager.Managers;
import manager.TaskManager;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создание задач с временными параметрами
        Task task1 = new Task(0, "Task 1", "Description 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 9, 4, 10, 0));
        Task task2 = new Task(0, "Task 2", "Description 2", Status.IN_PROGRESS,
                Duration.ofMinutes(45), LocalDateTime.of(2025, 9, 4, 11, 0));
        Task task3 = new Task(0, "Task 3", "Description 3", Status.DONE,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 9, 4, 12, 0));

        // Создание эпика с подзадачами
        Epic epic1 = new Epic(0, "Epic 1", "Description Epic 1", Status.NEW);
        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description Subtask 1", Status.NEW,
                epic1.getId(), Duration.ofMinutes(20),
                LocalDateTime.of(2025, 9, 4, 14, 0));
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description Subtask 2", Status.NEW,
                epic1.getId(), Duration.ofMinutes(30),
                LocalDateTime.of(2025, 9, 4, 15, 0));

        // Добавление задач в менеджер
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);
        manager.createEpic(epic1);
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Вывод списков задач
        System.out.println("Все задачи:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("\nВсе подзадачи:");
        manager.getAllSubtasks().forEach(System.out::println);

        System.out.println("\nВсе эпики:");
        manager.getAllEpics().forEach(System.out::println);

        // Изменение статуса подзадачи
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);

        System.out.println("\nСтатус эпика после изменения подзадачи:");
        System.out.println(epic1.getStatus()); // Должен быть IN_PROGRESS

        // Удаление задачи и эпика
        System.out.println(manager.getAllEpics());
        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("\nЗадачи после удаления:");
        manager.getAllTasks().forEach(System.out::println);

        System.out.println("\nЭпики после удаления:");
        System.out.println(manager.getAllEpics());
        manager.getAllEpics().forEach(System.out::println);
        System.out.println(" ");

        // Вывод приоритизированного списка
        System.out.println("\nПриоритизированные задачи:");
        manager.getPrioritizedTasks().forEach(System.out::println);

        // Проверка пересечений
        System.out.println("\nПроверка пересечений:");
        Task overlappingTask = new Task(0, "Overlapping Task", "Description", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 9, 4, 10, 30));
        System.out.println("Пересекается ли новая задача: " + manager.hasTimeOverlap(overlappingTask));

        //Отобразить в консоль tasks.csv
        File file = new File("tasks.csv");
        FileBackedTaskManager load = FileBackedTaskManager.loadFromFile(file);

        System.out.println(load.getTasks());
        System.out.println(load.getEpics());
        System.out.println(load.getSubtasks());
    }
}