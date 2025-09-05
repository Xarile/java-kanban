package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import manager.ManagerSaveException;
import models.Subtask;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            if (method.equals("GET")) {
                if (path.equals("/subtasks")) {
                    handleGetAllSubtasks(exchange);
                } else if (pathParts.length == 3 && pathParts[1].equals("subtasks") && isNumeric(pathParts[2])) {
                    handleGetSubtaskById(exchange, Integer.parseInt(pathParts[2]));
                } else {
                    sendNotFound(exchange);
                }
            } else if (method.equals("POST")) {
                if (path.equals("/subtasks")) {
                    handleCreateOrUpdateSubtask(exchange);
                } else {
                    sendNotFound(exchange);
                }
            } else if (method.equals("DELETE")) {
                if (path.equals("/subtasks")) {
                    handleDeleteAllSubtasks(exchange);
                } else if (pathParts.length == 3 && pathParts[1].equals("subtasks") && isNumeric(pathParts[2])) {
                    handleDeleteSubtaskById(exchange, Integer.parseInt(pathParts[2]));
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendError(exchange, e);
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        String response = gson.toJson(manager.getAllSubtasks());
        sendText(exchange, response, 200);
    }

    private void handleGetSubtaskById(HttpExchange exchange, int id) throws IOException {
        Subtask subtask = manager.getSubtask(id);

        if (subtask != null) {
            String response = gson.toJson(subtask);
            sendText(exchange, response, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        try {
            if (subtask.getId() == 0) {
                manager.createSubtask(subtask);
                sendText(exchange, "Подзадача создана", 201);
            } else {
                manager.updateSubtask(subtask);
                sendText(exchange, "Подзадача обновлена", 201);
            }
        } catch (ManagerSaveException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteAllSubtasks(HttpExchange exchange) throws IOException {
        manager.deleteAllSubtasks();
        sendText(exchange, "Все подзадачи удалены", 200);
    }

    private void handleDeleteSubtaskById(HttpExchange exchange, int id) throws IOException {
        manager.deleteSubtask(id);
        sendText(exchange, "Подзадача удалена", 200);
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}