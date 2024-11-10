/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package chatapp;

/**
 *
 * @author bruce
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatApp{
    private static final int PORT = 8080;
    private static Set<ClientHandler> clients = new HashSet<>();
    static Map<String, String> userColors = new HashMap<>();
    static Map<String, ClientHandler> usernameMap = new HashMap<>();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сървърът стартира на порт " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Ново свързване: " + socket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(socket);
                threadPool.submit(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Грешка при стартиране на сървъра: " + e.getMessage());
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void addClient(ClientHandler clientHandler) {
        clients.add(clientHandler);
        String color = getRandomColor();
        userColors.put(clientHandler.getUsername(), color);
        usernameMap.put(clientHandler.getUsername(), clientHandler);

        // Изпращане на уведомление за новия потребител
        broadcast(clientHandler.getUsername() + " се присъедини към чата!", null);

        clientHandler.sendMessage("Вашият цвят е: " + color);
    }

    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        usernameMap.remove(clientHandler.getUsername());
        userColors.remove(clientHandler.getUsername());

        // Изпращане на уведомление за напусналия потребител
        broadcast(clientHandler.getUsername() + " напусна чата.", null);
    }

    public static String getRandomColor() {
        String[] colors = {"#FF5733", "#33FF57", "#3357FF", "#F1C40F", "#8E44AD"};
        Random rand = new Random();
        return colors[rand.nextInt(colors.length)];
    }

    public static void sendPrivateMessage(String username, String message, ClientHandler sender) {
        ClientHandler recipient = usernameMap.get(username);
        if (recipient != null) {
            recipient.sendMessage("Лично съобщение от " + sender.getUsername() + ": " + message);
        } else {
            sender.sendMessage("Потребителят не е намерен.");
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Изчакване за име
            out.println("Въведете името си:");
            username = in.readLine();
            ChatApp.addClient(this);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/")) {
                    handleCommand(message);
                } else {
                    ChatApp.broadcast(formatMessage(message), this);
                }
            }
        } catch (IOException e) {
            System.out.println("Грешка при комуникация: " + e.getMessage());
        } finally {
            ChatApp.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void handleCommand(String command) {
        if (command.startsWith("/users")) {
            listUsers();
        } else if (command.startsWith("/message")) {
            String[] parts = command.split(" ", 3);
            if (parts.length == 3) {
                ChatApp.sendPrivateMessage(parts[1], parts[2], this);
            } else {
                sendMessage("Невалидна команда.");
            }
        } else if (command.startsWith("/help")) {
            sendMessage("/users - показва всички потребители.");
            sendMessage("/message username message - изпраща лично съобщение.");
            sendMessage("/help - показва тази помощ.");
        }
    }

    private void listUsers() {
        StringBuilder userList = new StringBuilder("Активни потребители:\n");
        for (String username : ChatApp.usernameMap.keySet()) {
            userList.append(username).append("\n");
        }
        sendMessage(userList.toString());
    }

    private String formatMessage(String message) {
        String color = ChatApp.userColors.get(username);
        return "<html><font color='" + color + "'>" + username + ": " + message + "</font></html>";
    }
}