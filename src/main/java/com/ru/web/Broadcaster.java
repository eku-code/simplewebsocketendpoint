package com.ru.web;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Broadcaster {

    private List<Socket> clientList;

    public Broadcaster () {
        clientList = new ArrayList<>();
    }

    public void addClient (Socket socket) {
        this.clientList.add(socket);
    }

    public synchronized void sendAll (String message) throws IOException {

        for (Socket socket : clientList) {
            socket.getOutputStream().write(Encoder.encode(message));
        }
    }

    public synchronized void removeClient (Socket socket) {
        clientList.remove(socket);
    }
}
