package InstantMessaging;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class IMClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 3000;
    private String clientID;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private DefaultListModel<String> clientListModel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JList<String> clientList;
    private Map<String, List<String>> chatHistory;
    private JButton sendButton;

    public IMClient(String clientID) {
        this.clientID = clientID;
        this.chatHistory = new HashMap<>();
        createGUI();
    }

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF(clientID);
            out.flush();

            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        try {
            while (true) {
                String message = in.readUTF();

                if (message.startsWith("CLIENT_LIST:")) {
                    // Update client list
                    String[] clients = message.substring(12).split(",");
                    SwingUtilities.invokeLater(() -> {
                        clientListModel.clear();
                        for (String client : clients) {
                            if (!client.equals(clientID)) {
                                clientListModel.addElement(client);
                            }
                        }
                    });
                } else {
                    int separatorIndex = message.indexOf(": ");
                    if (separatorIndex != -1) {
                        String sender = message.substring(0, separatorIndex);
                        String text = message.substring(separatorIndex + 2);

                        chatHistory.putIfAbsent(sender, new ArrayList<>());
                        chatHistory.get(sender).add(sender + ": " + text);

                        if (sender.equals(clientList.getSelectedValue())) {
                            updateChatDisplay(sender);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Disconnected from server.");
        }
    }


    private void sendMessage() {
        String receiverID = clientList.getSelectedValue();
        if (receiverID == null) {
            JOptionPane.showMessageDialog(null, "Select a recipient!");
            return;
        }

        String messageText = messageField.getText();
        if (messageText.isEmpty()) return;

        try {
            out.writeUTF(clientID + ":" + receiverID + ":" + messageText);
            out.flush();

            chatHistory.putIfAbsent(receiverID, new ArrayList<>());
            chatHistory.get(receiverID).add("Me: " + messageText);

            updateChatDisplay(receiverID);
            messageField.setText("");
        } catch (IOException e) {
            System.err.println("Error sending message.");
        }
    }

    private void updateChatDisplay(String user) {
        chatArea.setText("");
        if (chatHistory.containsKey(user)) {
            for (String msg : chatHistory.get(user)) {
                chatArea.append(msg + "\n");
            }
        }
    }

    private void createGUI() {
        JFrame frame = new JFrame("Instant Messenger - " + clientID);
        frame.setSize(450, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane clientScroll = new JScrollPane(clientList);
        clientScroll.setPreferredSize(new Dimension(150, 0));

        clientList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = clientList.getSelectedValue();
                if (selectedUser != null) {
                    updateChatDisplay(selectedUser);
                }
            }
        });

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        messageField = new JTextField();
        messageField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientScroll, chatScroll);
        splitPane.setDividerLocation(150);
        splitPane.setResizeWeight(0.3);

        frame.setLayout(new BorderLayout());
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }


    public static void main(String[] args) {
        String clientID;
        do {
            clientID = JOptionPane.showInputDialog("Enter your ID (Required):");
            if (clientID == null) {
                int exit = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
                if (exit == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        } while (clientID == null || clientID.trim().isEmpty());
        IMClient client = new IMClient(clientID);
        client.start();
    }
}
