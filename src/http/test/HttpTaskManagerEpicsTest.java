package http.test;

import models.Epic;
import models.Status;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicsTest extends HttpTestBase {

    @Test
    public void testCreateEpic() throws Exception {
        Epic epic = new Epic(0, "Test Epic", "Test Description", Status.NEW);
        String epicJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Epic> epics = manager.getAllEpics();
        assertNotNull(epics);
        assertEquals(1, epics.size());
        assertEquals("Test Epic", epics.get(0).getName());
    }

    @Test
    public void testGetEpicById() throws Exception {
        Epic epic = new Epic(0, "Test Epic", "Test Description", Status.NEW);
        int epicId = manager.createEpic(epic);

        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic responseEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(epicId, responseEpic.getId());
        assertEquals("Test Epic", responseEpic.getName());
    }

    @Test
    public void testGetEpicSubtasks() throws Exception {
        Epic epic = new Epic(0, "Test Epic", "Test Description", Status.NEW);
        int epicId = manager.createEpic(epic);

        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    public void testDeleteEpic() throws Exception {
        Epic epic = new Epic(0, "Test Epic", "Test Description", Status.NEW);
        int epicId = manager.createEpic(epic);

        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertNull(manager.getEpic(epicId));
    }
}