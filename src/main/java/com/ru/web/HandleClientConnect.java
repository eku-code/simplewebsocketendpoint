package com.ru.web;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HandleClientConnect extends Thread {
    private Socket socket;
    private Broadcaster broadcaster;

    public HandleClientConnect(Socket socket, Broadcaster broadcaster) {
        this.socket = socket;
        this.broadcaster = broadcaster;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            try {
                doHandShakeToInitializeWebSocketConnection(input, output);
            } catch (UnsupportedEncodingException handShakeException) {
                System.out.println("Error during handshake: " + handShakeException.getMessage());
            }

          /*  try {
                output.write(Encoder.encode("Hello from Server!"));
            } catch (UnsupportedEncodingException e) {
                System.out.println("Error: " + e.getMessage());
            } */

            try {
                printInputStream(input);
            } catch (IOException printException) {
                System.out.println("Error reading input stream: " + printException.getMessage());
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
        }
    }

    private void printInputStream(InputStream inputStream ) throws IOException {
        int len = 0;
        byte[] b = new byte[1024];
        String msgResp;
        //rawIn is a Socket.getInputStream();
        while(true){
            len = inputStream.read(b);
            if(len!=-1){

                byte rLength = 0;
                int rMaskIndex = 2;
                int rDataStart = 0;
                //b[0] is always text in my case so no need to check;
                byte data = b[1];
                byte op = (byte) 127;
                rLength = (byte) (data & op);

                if(rLength==(byte)126) rMaskIndex=4;
                if(rLength==(byte)127) rMaskIndex=10;

                byte[] masks = new byte[4];

                int j=0;
                int i=0;
                for(i=rMaskIndex;i<(rMaskIndex+4);i++){
                    masks[j] = b[i];
                    j++;
                }

                rDataStart = rMaskIndex + 4;

                int messLen = len - rDataStart;

                byte[] message = new byte[messLen];

                for(i=rDataStart, j=0; i<len; i++, j++){
                    message[j] = (byte) (b[i] ^ masks[j % 4]);
                }

                msgResp = new String(message);
                System.out.println(msgResp);
                if (msgResp.contains("Disconnect")) {
                    socket.close();
                    broadcaster.removeClient(socket);
                } else {
                    broadcaster.sendAll(msgResp);
                }

                b = new byte[1024];
            }
        }
    }

    private static void doHandShakeToInitializeWebSocketConnection(InputStream inputStream, OutputStream outputStream) throws UnsupportedEncodingException {
        String data = new Scanner(inputStream,"UTF-8").useDelimiter("\\r\\n\\r\\n").next();

        Matcher get = Pattern.compile("^GET").matcher(data);

        if (get.find()) {
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();

            byte[] response = null;
            try {
                response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + DatatypeConverter.printBase64Binary(
                        MessageDigest
                                .getInstance("SHA-1")
                                .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                        .getBytes("UTF-8")))
                        + "\r\n\r\n")
                        .getBytes("UTF-8");
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                outputStream.write(response, 0, response.length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}