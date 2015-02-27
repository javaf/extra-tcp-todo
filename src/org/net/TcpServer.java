// @wolfram77
package org.net;

// required modules
import java.util.concurrent.*;
import java.util.*;
import java.net.*;
import java.io.*;
import org.event.*;



public class TcpServer extends Thread {

    // data
    Map<InetSocketAddress, TcpClient> clients;
    BlockingQueue<NetPkt> rxPkts;
    InetSocketAddress addr;
    ServerSocket socket;
    EventEmitter event;
    
    
    // TcpServer (event, link, tx, rx)
    // - create tcp server with given event emitter
    public TcpServer(EventEmitter event, Object link, BlockingQueue<NetPkt> rxPkts) throws IOException {
        clients = new ConcurrentHashMap<>();
        if(event != null) this.event = event;
        else this.event = new EventEmitter();
        if(link instanceof ServerSocket) socket = (ServerSocket)link;
        else if(link instanceof Integer) socket = new ServerSocket((Integer)link);
        else socket = new ServerSocket();
        addr = Inet.addr(socket);
        this.rxPkts = rxPkts;
    }
    
    
    // Event ()
    // - returns the event emitter
    public EventEmitter event() {
        return event;
    }
    
    
    // Addr ()
    // - returns server address
    public InetSocketAddress addr() {
        return addr;
    }
    
    
    // Socket ()
    // - return server socket
    public ServerSocket socket() {
        return socket;
    }
    
    
    // RxPkts ()
    // - returns received packet queue
    public BlockingQueue<NetPkt> rxPkts() {
        return rxPkts;
    }
    
    
    // Clients ()
    // - returns clients map
    public Map<InetSocketAddress, TcpClient> clients() {
        return clients;
    }
    
    
    // Close ()
    // - closes server and all connections
    public void close() throws IOException {
        for(InetSocketAddress adrs : clients.keySet())
            clients.get(adrs).close();
        socket.close();
    }
    
    
    // Read ()
    // - read data from a client (blocking)
    public NetPkt read() throws InterruptedException {
        return rxPkts.take();
    }
    
    
    // Write ()
    // - write data to a specific or all clients (null)
    public TcpServer write(NetPkt pkt) throws InterruptedException {
        if(pkt.addr() != null) { clients.get(pkt.addr()).write(pkt); return this; }
        for(InetSocketAddress adrs : clients.keySet())
            clients.get(adrs).write(pkt);
        return this;
    }

    
    // Add (link)
    // - add a client
    public TcpClient add(Object link) throws IOException {
        InetSocketAddress adrs = Inet.addr(link);
        if(clients.containsKey(adrs)) return clients.get(adrs);
        TcpClient client = new TcpClient(event, link, rxPkts);
        client.start();
        clients.put(adrs, client);
        if(link instanceof InetSocketAddress)
            event.emit("connect", "addr", adrs);
        return client;
    }
    
    
    // Remove (link)
    // - remove a client
    public TcpServer remove(Object link) throws IOException {
        InetSocketAddress adrs = Inet.addr(link);
        if(!clients.containsKey(adrs)) return this;
        clients.get(adrs).close();
        clients.remove(adrs);
        event.emit("disconnect", "addr", adrs);
        return this;
    }
    
    
    // RemoveCheck (addr)
    // - removes a client if it disconnected
    public void removeCheck(Object link) throws IOException {
        InetSocketAddress adrs = Inet.addr(link);
        if(!clients.containsKey(adrs)) return;
        if(clients.get(adrs).socket().isConnected()) return;
        remove(adrs);
    }
    
    
    // AcceptAction ()
    // - accept incoming connections
    public void acceptAction() throws IOException {
        while(!socket.isClosed()) {
            try {
                TcpClient client = add(socket.accept());
                event.emit("accept", "client", client);
            }
            catch(Exception e) { event.emit("accept-error", "err", e, "server", this); }
        }
    }
    
    
    // Run ()
    // - accept incoming clients / send pending data
    @Override
    public void run() {
        try { acceptAction(); }
        catch(IOException e) {}
        event.emit("unaccept", "server", this);
    }
}
