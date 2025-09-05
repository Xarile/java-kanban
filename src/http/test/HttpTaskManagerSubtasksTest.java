package http.test;

import models.Epic;
import models.Status;
import models.Subtask;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest extends HttpTestBase {

    @Test
    public void testCreateSubtask() throws Exception {
        Epic epic = new Epic(0, "Test Epic", "Test Description", Status.NEW);
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Test Subtask", "Test Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> subtasks = manager.getAllSubtasks();
        assertNotNull(subtasks);
        assertEquals(1, subtasks.size());
        assertEquals("Test Subtask", subtasks.get(0).getName());
    }

    @Test
    public void testGetSubtaskById() throws Exception {
        Epic epic = new Epic(0, "Test Epic", "Test Description", Status.NEW);
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Test Subtask", "Test Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        int subtaskId = manager.createSubtask(subtask);

        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask responseSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtaskId, responseSubtask.getId());
        assertEquals("Test Subtask", responseSubtask.getName());
    }

    @Test
    public void testUpdateSubtask() throws Exception {
        Epic epic = new Epic(0, "Test Epic", "Test Description", Status.NEW);
        int epicId = manager.createEpic(epic);

        Subtask subtask = new Subtask(0, "Test Subtask", "Test Description", Status.NEW, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
        int subtaskId = manager.createSubtask(subtask);

        Subtask updatedSubtask = new Subtask(subtaskId, "Updated Subtask", "Updated Description", Status.DONE, epicId,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));
        String subtaskJson = gson.toJson(updatedSubtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Subtask storedSubtask = manager.getSubtask(subtaskId);
        assertEquals("Updated Subtask", storedSubtask.getName());
        assertEquals(Status.DONE, storedSubtask.getStatus());
    }
}