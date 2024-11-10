/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package clientchatapp;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientChatApp {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static JTextPane messageArea;
    private static JTextField inputField;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Многопотребителски Чат");
        frame.setLayout(new BorderLayout());
        messageArea = new JTextPane();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        frame.add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(e -> sendMessage());

        JButton sendButton = new JButton("Изпрати");
        sendButton.addActionListener(e -> sendMessage());
        frame.add(sendButton, BorderLayout.EAST);

        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        connectToServer();
    }

    public static void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Въвеждаме име на потребителя
            String username = JOptionPane.showInputDialog("Въведете вашето име:");
            out.println(username);

            Thread readThread = new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        displayMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage() {
        String message = inputField.getText();
        out.println(message);
        inputField.setText("");
    }

    public static void displayMessage(String message) {
        messageArea.setText(messageArea.getText() + "\n" + message);
    }
}