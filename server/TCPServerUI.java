package server;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class TCPServerUI extends JFrame {

    private JTextArea logArea;
    private JTextField inputField;
    private JButton sendButton;
    private ServerSocket serverSocket;
    private ArrayList<Socket> clients = new ArrayList<>();
    private boolean isRunning = false;

    public TCPServerUI() {
        setTitle("TCP Server UI");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // LOG AREA
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        add(scroll, BorderLayout.CENTER);

        // INPUT AREA
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // START SERVER
        startServer();

        // SEND BUTTON ACTION
        sendButton.addActionListener(e -> sendMessageToClients());
        inputField.addActionListener(e -> sendMessageToClients());
    }

    private void startServer() {
        if (isRunning) return;
        isRunning = true;
        appendLog("Starting server on port 12345...");

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(12345);
                appendLog("Server started! Waiting for clients...");

                while (true) {
                    Socket client = serverSocket.accept();
                    clients.add(client);
                    appendLog("Client connected: " + client.getInetAddress());

                    new Thread(new ClientHandler(client)).start();
                }
            } catch (IOException ex) {
                appendLog("Server error: " + ex.getMessage());
            }
        }).start();
    }

    private void sendMessageToClients() {
        String msg = inputField.getText();
        if (!msg.isEmpty()) {
            appendLog("Server: " + msg);
            for (Socket client : clients) {
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println("Server: " + msg);
                } catch (IOException ex) {
                    appendLog("Error sending message to client.");
                }
            }
            inputField.setText("");
        }
    }

    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    // CLIENT HANDLER
    class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    appendLog("Client says: " + message);
                    broadcast(message, socket); // <-- corrected: pass sender
                }

            } catch (IOException ex) {
                appendLog("Client disconnected.");
            } finally {
                clients.remove(socket);
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        private void broadcast(String message, Socket sender) {
            for (Socket client : clients) {
                if (client == sender) continue; // skip sending to sender
                try {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println(message); // send plain message to others
                } catch (IOException e) {
                    appendLog("Failed to send message to a client.");
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TCPServerUI().setVisible(true));
    }
}
