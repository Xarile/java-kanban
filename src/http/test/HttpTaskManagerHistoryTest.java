package http.test;

import models.Status;
import models.Task;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerHistoryTest extends HttpTestBase {

    @Test
    public void testGetHistory() throws Exception {
        Task task = new Task(0, "Test Task", "Test Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = manager.createTask(task);
        manager.getTask(taskId);

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertFalse(response.body().isEmpty());
        assertTrue(response.body().contains("Test Task"));
    }

    @Test
    public void testGetEmptyHistory() throws Exception {
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }
}