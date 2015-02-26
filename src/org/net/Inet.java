// @wolfram77
package org.net;

// required modules
import java.net.*;



public class Inet {
    
    
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
        return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
    }
    
    
    // Addr (socket)
    // - get socket address from server-socket
    public static InetSocketAddress addr(ServerSocket socket) {
        return new InetSocketAddress(socket.getInetAddress(), socket.getLocalPort());
    }
}
