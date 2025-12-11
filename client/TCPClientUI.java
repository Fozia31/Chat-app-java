package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class TCPClientUI extends JFrame {

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendBtn;
    private PrintWriter out;

    public TCPClientUI() {
        setTitle("TCP Client UI");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // CHAT AREA
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(chatArea);
        add(scroll, BorderLayout.CENTER);

        // INPUT AREA
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendBtn = new JButton("Send");
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        connectToServer();

        // SEND ACTION
        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        chatArea.append(msg + "\n");
                    }
                } catch (IOException e) {
                    chatArea.append("Disconnected from server.\n");
                }
            }).start();

        } catch (IOException e) {
            chatArea.append("Error connecting to server.\n");
        }
    }

    private void sendMessage() {
        String msg = inputField.getText();
        if (!msg.isEmpty()) {
            out.println(msg);
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TCPClientUI().setVisible(true));
    }
}
