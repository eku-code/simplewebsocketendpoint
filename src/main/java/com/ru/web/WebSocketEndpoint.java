package com.ru.web;

import java.io.*;
import java.net.*;


public class WebSocketEndpoint {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(8094)) {

            System.out.println("Server is listening");
            System.out.println(serverSocket.toString());
            Broadcaster broadcaster = new Broadcaster();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                broadcaster.addClient(socket);
                new HandleClientConnect(socket, broadcaster).start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}