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
    BlockingQueue<NetPkt> tx;
    BlockingQueue<NetPkt> rx;
    InetSocketAddress addr;
    ServerSocket socket;
    EventEmitter event;
    boolean txHelper;
    
    
    // Copy (srv)
    // - copy object data from another object
    private void copy(TcpServer srv) {
        this.clients = srv.clients;
        this.tx = srv.tx;
        this.rx = srv.rx;
        this.addr = srv.addr;
        this.socket = srv.socket;
        this.event = srv.event;
        this.txHelper = srv.txHelper;
    }
    
    
    // GetSocket (link)
    // - get server socket from link
    private ServerSocket getSocket(Object link) throws IOException {
        if(link == null) return new ServerSocket();
        if(link instanceof ServerSocket) return (ServerSocket)link;
        if(link instanceof Integer) return new ServerSocket((Integer)link);
        return null;
    }
    
    
    // Init (event, link, tx, rx)
    // - initialize tcp server
    private void init(EventEmitter event, Object link, BlockingQueue<NetPkt> tx, BlockingQueue<NetPkt> rx) throws IOException {
        // prepare socket
        socket = getSocket(link);
        if(socket == null) {
            copy((TcpServer)link);
            ((TcpServer)link).txHelper = true;
            this.start();
            return;
        }
        // init remaining fields
        if(event != null) this.event = event;
        else this.event = new EventEmitter();
        clients = new ConcurrentHashMap<>();
        addr = Inet.addr(socket);
        this.tx = tx;
        this.rx = rx;
        this.start();
        // start tx thread if necessary
        if(tx == null) return;
        (new TcpServer(event, this, tx, rx)).start();
    }
    
    
    // TcpServer (event, link, tx, rx)
    // - create tcp server with given event emitter
    public TcpServer(EventEmitter event, Object link, BlockingQueue<NetPkt> tx, BlockingQueue<NetPkt> rx) throws IOException {
        init(event, link, tx, rx);
    }
    
    
    // TcpServer (event, link, tx, rx)
    // - create tcp server with independent event emitter
    public TcpServer(Object link, BlockingQueue<NetPkt> tx, BlockingQueue<NetPkt> rx) throws IOException {
        init(null, link, tx, rx);
    }
    
    
    // AcceptAction ()
    // - accept incoming connections
    public void acceptAction() throws IOException {
        while(!socket.isClosed()) {
            Socket sckt = socket.accept();
            TcpClient client = new TcpClient(event, sckt, tx, rx);
            clients.put(Inet.addr(sckt), client);
            event.emit("accept", "client", client);
        }
    }
    
    
    // WriteAction ()
    // - write pending packets to clients
    public void writeAction() throws IOException, InterruptedException {
        while(!socket.isClosed()) {
            NetPkt pkt = tx.take();
            if(!clients.containsKey(pkt.addr)) {
                TcpClient client = new TcpClient(pkt.addr, tx, rx);
                clients.put(pkt.addr, client);
                event.emit("connect", "client", client);
            }
            // write how?
            
        }
    }
    
    @Override
    public void run() {
        try {
            acceptAction();
        }
        catch(Exception e) { System.out.println(e); }
    }
    
    public void close() throws IOException {
        socket.close();
        for(InetSocketAddress clientAddr : clients.keySet())
            clients.get(clientAddr).close();
    }
}
