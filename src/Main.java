public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создание задач
        Task task1 = new Task(0, "Task 1", "Description 1", Status.NEW);
        Task task2 = new Task(0, "Task 2", "Description 2", Status.IN_PROGRESS);
        Task task3 = new Task(0,"Task 3 ", "Discription 3", Status.DONE);

        // Создание эпика с подзадачами
        Epic epic1 = new Epic(0, "Epic 1", "Description Epic 1", Status.NEW);
        Subtask subtask1 = new Subtask(0, "Subtask 1", "Description Subtask 1", Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(0, "Subtask 2", "Description Subtask 2", Status.NEW, epic1.getId());


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
    }
}