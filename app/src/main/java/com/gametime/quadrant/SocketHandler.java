package com.gametime.quadrant;

import java.net.Socket;

/**
 * Created by Akansh on 30-04-2018.
 */

public class SocketHandler {
    private static Socket socket;

    public static synchronized Socket getSocket() {
        return socket;
    }

    public static synchronized void setSocket(Socket socket) {
        SocketHandler.socket = socket;
    }
}
