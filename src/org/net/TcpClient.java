// @wolfram77
package org.net;

// required modules
import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import org.event.*;



public class TcpClient extends Thread {
    
    // data
    InetSocketAddress addr;
    EventEmitter event;
    SocketWriter out;
    SocketReader in;
    Socket socket;
    
    
    // TcpClient (event, socket, tx, rx)
    // - create tcp client with existing event emitter
    public TcpClient(EventEmitter event, Object link, BlockingQueue<NetPkt> rxPkts) throws IOException {
        if(link instanceof Socket) socket = (Socket)link;
        else socket = Inet.connect((InetSocketAddress)link);
        if(event != null) this.event = event;
        else this.event = new EventEmitter();
        in = new SocketReader(this.event, socket, rxPkts);
        out = new SocketWriter(this.event, socket, null);
        addr = Inet.addr(socket);
    }
    
    
    // Event ()
    // - get event emitter
    public EventEmitter event() {
        return event;
    }
    
    
    // Out ()
    // - returns socket writer
    public SocketWriter out() {
        return out;
    }
    
    
    // In ()
    // - returns socket reader
    public SocketReader in() {
        return in;
    }
    
    
    // Socket ()
    // - get client socket
    public Socket socket() {
        return socket;
    }
    
    
    // Addr ()
    // - get client address
    public InetSocketAddress addr() {
        return addr;
    }
    
    
    // Close ()
    // - close client socket
    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
        event.emit("close", "addr", addr);
    }
    
    
    // Start ()
    // - start read and write threads
    @Override
    public void start() {
        out.start();
        in.start();
    }
    
    
    // Read ()
    // - read data from client (blocking)
    public NetPkt read() throws InterruptedException {
        return in.pkts().take();
    }
    
    
    // Write (pkt)
    // - write data to client
    public TcpClient write(NetPkt pkt) throws InterruptedException {
        out.pkts().put(pkt);
        return this;
    }
    
    
    // Write (data)
    // - write data to client
    public TcpClient write(byte[] data) throws InterruptedException {
        out.pkts().put(new NetPkt(addr, data));
        return this;
    }
}
