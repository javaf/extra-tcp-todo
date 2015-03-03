// @wolfram77
package org.net;

// required modules
import java.util.concurrent.*;
import java.net.*;
import java.nio.*;
import java.io.*;
import org.event.*;



public class SocketWriter extends Thread {
    
    // data
    BlockingQueue<NetPkt> pkts;
    InetSocketAddress addr;
    EventEmitter event;
    OutputStream out;
    ByteBuffer buff;
    Socket socket;
    
    
    // SocketReader (event, socket)
    // - creates a socket reader
    public SocketWriter(EventEmitter event, Socket socket, BlockingQueue<NetPkt> pkts) throws IOException {
        if(pkts != null) this.pkts = pkts;
        else this.pkts = new LinkedBlockingQueue<>();
        if(event != null) this.event = event;
        else this.event = new EventEmitter();
        buff = ByteBuffer.allocate(4);
        buff.order(ByteOrder.BIG_ENDIAN);
        out = socket.getOutputStream();
        addr = Inet.addr(socket);
        this.socket = socket;
    }
    
    
    // Pkts ()
    // - returns queue containing data
    public BlockingQueue<NetPkt> pkts() {
        return pkts;
    }
    
    
    // Event ()
    // - returns the event emitter
    public EventEmitter event() {
        return event;
    }
    
    
    // Socket ()
    // - returns the socket
    public Socket socket() {
        return socket;
    }
    
    
    // Addr ()
    // - returns the socket address
    public InetSocketAddress addr() {
        return addr;
    }
    
    
    // Close ()
    // - close socket writer
    public void close() throws IOException {
        socket.shutdownOutput();
        out.close();
        event.emit("close-write", "addr", addr);
    }
    
    
    // WriteAction ()
    // - write pending packets
    private void writeAction() throws IOException, InterruptedException {
        while(!socket.isClosed()) {
            NetPkt pkt = pkts.take();
            int size = pkt.data().length;
            buff.putInt(0, size);
            out.write(buff.array());
            out.write(pkt.data());
            event.emit("write", "pkt", pkt);
        }
    }
    
    
    // Run ()
    // - write packets to socket
    @Override
    public void run() {
        try { writeAction(); }
        catch(IOException | InterruptedException e) {}
        event.emit("disconnect", "addr", addr);
    }
}
