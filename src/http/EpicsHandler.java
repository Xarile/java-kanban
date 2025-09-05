package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import models.Epic;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
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
                if (path.equals("/epics")) {
                    handleGetAllEpics(exchange);
                } else if (pathParts.length == 3 && pathParts[1].equals("epics") && isNumeric(pathParts[2])) {
                    handleGetEpicById(exchange, Integer.parseInt(pathParts[2]));
                } else if (pathParts.length == 4 && pathParts[1].equals("epics") && isNumeric(pathParts[2]) && pathParts[3].equals("subtasks")) {
                    handleGetEpicSubtasks(exchange, Integer.parseInt(pathParts[2]));
                } else {
                    sendNotFound(exchange);
                }
            } else if (method.equals("POST")) {
                if (path.equals("/epics")) {
                    handleCreateOrUpdateEpic(exchange);
                } else {
                    sendNotFound(exchange);
                }
            } else if (method.equals("DELETE")) {
                if (path.equals("/epics")) {
                    handleDeleteAllEpics(exchange);
                } else if (pathParts.length == 3 && pathParts[1].equals("epics") && isNumeric(pathParts[2])) {
                    handleDeleteEpicById(exchange, Integer.parseInt(pathParts[2]));
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

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        String response = gson.toJson(manager.getAllEpics());
        sendText(exchange, response, 200);
    }

    private void handleGetEpicById(HttpExchange exchange, int id) throws IOException {
        Epic epic = manager.getEpic(id);

        if (epic != null) {
            String response = gson.toJson(epic);
            sendText(exchange, response, 200);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, int id) throws IOException {
        String response = gson.toJson(manager.getSubtasksByEpicId(id));
        sendText(exchange, response, 200);
    }

    private void handleCreateOrUpdateEpic(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic.getSubtaskIds() == null) {
            epic.setSubtaskIds(new ArrayList<>());
        }

        if (epic.getId() == 0) {
            manager.createEpic(epic);
            sendText(exchange, "Эпик создан", 201);
        } else {
            manager.updateEpic(epic);
            sendText(exchange, "Эпик обновлен", 201);
        }
    }

    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        manager.deleteAllEpics();
        sendText(exchange, "Все эпики удалены", 200);
    }

    private void handleDeleteEpicById(HttpExchange exchange, int id) throws IOException {
        manager.deleteEpic(id);
        sendText(exchange, "Эпик удален", 200);
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