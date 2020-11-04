package com.example.smartphonetovirtuality;

import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.io.IOException;

public class Client {
    public void sendTCP(String msg, String ip, int port) {
        Socket socket;
        try {
            socket = new Socket(ip, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(msg);
            socket.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void sendUDP(String msg, String ip, int port) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), port);
            socket.send(packet);
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

