// @wolfram77
package main;

// required modules
import java.util.*;
import java.net.*;
import org.net.*;


public class Main {
    
    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        NetTell netev = new NetTell();
        TcpServer server = new TcpServer(null, 80, null);
        server.event().add("read", netev);
        server.event().add("write", netev);
        server.start();
        Thread.sleep(100);
        TcpClient client = new TcpClient(null, new InetSocketAddress("127.0.0.1", 80), null);
        client.event().add("read", netev);
        client.event().add("write", netev);
        client.start();
        Thread.sleep(100);
        client.write(new byte[] {1, 2, 3});
        server.write(new byte[] {1, 2, 3, 4});
        System.out.println(server.clients());
        in.next();
        in.close();
    }
}
