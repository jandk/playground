package be.twofold.playground.socket;

import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        try (ServerSocket serverSocket = new ServerSocket(12345);
             Socket socket = serverSocket.accept()
        ) {
            System.out.println("Connection established");

            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                writer.println("Hello from server in a string");
            }
        }
    }
}
