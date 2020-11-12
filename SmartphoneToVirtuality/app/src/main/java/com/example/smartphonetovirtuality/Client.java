package com.example.smartphonetovirtuality;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UPD client which sends data to a UDP server.
 * @author COGOLUEGNES Charles
 */
public class Client {
    /**
     * Sends a message via a datagram packet through a datagram socket.
     * @param msg a String which to be sent.
     * @param ip the ip address of the server.
     * @param port the port of the server.
     */
    public static void sendUDP(String msg, String ip, int port) {
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

