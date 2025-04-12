package InstantMessaging;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

class ServerWorker extends Thread {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String clientID;
    private static Map<String, ServerWorker> clients = new HashMap<>();

    public ServerWorker(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            clientID = in.readUTF();
            registerClient(clientID, this);

            while (true) {
                String message = in.readUTF();
                if (message.equalsIgnoreCase("EXIT")) {
                    break;
                }

                String[] parts = message.split(":", 3);
                if (parts.length == 3) {
                    String senderID = parts[0];
                    String receiverID = parts[1];
                    String messageText = parts[2];

                    sendMessageToClient(receiverID, senderID + ": " + messageText);
                }
            }
        } catch (IOException e) {
            System.err.println(clientID + " disconnected.");
        } finally {
            removeClient(clientID);
            closeConnections();
        }
    }

    static synchronized void registerClient(String clientID, ServerWorker worker) {
        clients.put(clientID, worker);
        broadcastClientList();
    }

    static synchronized void removeClient(String clientID) {
        clients.remove(clientID);
        broadcastClientList();
    }

    static synchronized void sendMessageToClient(String receiverID, String message) {
        ServerWorker receiver = clients.get(receiverID);
        if (receiver != null) {
            receiver.sendMessage(message);
        }
    }

    private static void broadcastClientList() {
        String clientList = String.join(",", clients.keySet());
        for (ServerWorker client : clients.values()) {
            client.sendMessage("CLIENT_LIST:" + clientList);
        }
    }

    void sendMessage(String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message.");
        }
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
