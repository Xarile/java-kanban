package http.test;

import models.Status;
import models.Task;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest extends HttpTestBase {

    @Test
    public void testGetAllTasksWhenEmpty() throws Exception {
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    public void testCreateTask() throws Exception {
        Task task = new Task(0, "Test Task", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasks = manager.getAllTasks();
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getName());
    }

    @Test
    public void testGetTaskById() throws Exception {
        Task task = new Task(0, "Test Task", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(task);

        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertEquals(taskId, responseTask.getId());
        assertEquals("Test Task", responseTask.getName());
    }

    @Test
    public void testGetTaskByIdNotFound() throws Exception {
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateTask() throws Exception {
        Task task = new Task(0, "Test Task", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(task);

        Task updatedTask = new Task(taskId, "Updated Task", "Updated Description", Status.DONE,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));
        String taskJson = gson.toJson(updatedTask);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Task storedTask = manager.getTask(taskId);
        assertEquals("Updated Task", storedTask.getName());
        assertEquals(Status.DONE, storedTask.getStatus());
    }

    @Test
    public void testDeleteTask() throws Exception {
        Task task = new Task(0, "Test Task", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(task);

        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getTask(taskId));
    }

    @Test
    public void testDeleteAllTasks() throws Exception {
        Task task1 = new Task(0, "Test Task 1", "Test Description 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task(0, "Test Task 2", "Test Description 2", Status.IN_PROGRESS,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));
        manager.createTask(task1);
        manager.createTask(task2);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    public void testTaskTimeOverlap() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(0, "Test Task 1", "Test Description 1", Status.NEW,
                Duration.ofMinutes(30), now);
        manager.createTask(task1);

        Task task2 = new Task(0, "Test Task 2", "Test Description 2", Status.NEW,
                Duration.ofMinutes(30), now.plusMinutes(15));
        String taskJson = gson.toJson(task2);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }
}