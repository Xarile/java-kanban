package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import manager.ManagerSaveException;
import models.Task;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
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
                if (path.equals("/tasks")) {
                    handleGetAllTasks(exchange);
                } else if (pathParts.length == 3 && pathParts[1].equals("tasks") && isNumeric(pathParts[2])) {
                    handleGetTaskById(exchange, Integer.parseInt(pathParts[2]));
                } else {
                    sendNotFound(exchange);
                }
            } else if (method.equals("POST")) {
                if (path.equals("/tasks")) {
                    handleCreateOrUpdateTask(exchange);
                } else {
                    sendNotFound(exchange);
                }
            } else if (method.equals("DELETE")) {
                if (path.equals("/tasks")) {
                    handleDeleteAllTasks(exchange);
                } else if (pathParts.length == 3 && pathParts[1].equals("tasks") && isNumeric(pathParts[2])) {
                    handleDeleteTaskById(exchange, Integer.parseInt(pathParts[2]));
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

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        String response = gson.toJson(manager.getAllTasks());
        sendText(exchange, response, 200);
    }

    private void handleGetTaskById(HttpExchange exchange, int id) throws IOException {
        Task task = manager.getTask(id);

        if (task != null) {
            String response = gson.toJson(task);
            sendText(exchange, response, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);

        try {
            if (task.getId() == 0) {
                manager.createTask(task);
                sendText(exchange, "Задача создана", 201);
            } else {
                manager.updateTask(task);
                sendText(exchange, "Задача обновлена", 201);
            }
        } catch (ManagerSaveException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        manager.deleteAllTasks();
        sendText(exchange, "Все задачи удалены", 200);
    }

    private void handleDeleteTaskById(HttpExchange exchange, int id) throws IOException {
        manager.deleteTask(id);
        sendText(exchange, "Задача удалена", 200);
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