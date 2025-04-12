package InstantMessaging;

import java.io.*;
import java.net.*;
import java.util.*;

public class IMServer {
    private static final int PORT = 3000;

    public static void main(String[] args) {
        System.out.println("Server is running...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ServerWorker(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}