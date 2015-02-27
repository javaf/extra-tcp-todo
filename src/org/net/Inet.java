// @wolfram77
package org.net;

// required modules
import java.net.*;
import java.io.*;



public class Inet {

    // constants
    final static int timeout = 10000;

    // errors
    final static String eNoConnection = "Connection not Established";


    // Addr (addr)
    // - get socket address from string
    public static InetSocketAddress addr(String addr) {
        String[] parts = addr.split(":");
        String ipAddr = parts[0];
        int port = Integer.parseInt(parts[1]);
        return new InetSocketAddress(ipAddr, port);
    }


    // Addr (socket)
    // - get socket address from socket
    public static InetSocketAddress addr(Socket socket) {
        return (InetSocketAddress)socket.getRemoteSocketAddress();
    }


    // Addr (socket)
    // - get socket address from server-socket
    public static InetSocketAddress addr(ServerSocket socket) {
        return (InetSocketAddress)socket.getLocalSocketAddress();
    }


    // Connect (addr)
    // - connect to a given (server) address
    public static Socket connect(InetSocketAddress addr) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(timeout);
        socket.connect(addr);
        if(!socket.isConnected())
            throw new SocketException(eNoConnection);
        return socket;
    }
}
