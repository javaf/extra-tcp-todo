// @wolfram77
package org.net;

// required modules
import java.util.concurrent.*;
import java.net.*;
import java.nio.*;
import java.io.*;
import org.event.*;



public class SocketReader extends Thread {
    
    // data
    BlockingQueue<NetPkt> pkts;
    InetSocketAddress addr;
    EventEmitter event;
    ByteBuffer buff;
    InputStream in;
    Socket socket;
    
    
    // SocketReader (event, socket)
    // - creates a socket reader
    public SocketReader(EventEmitter event, Socket socket, BlockingQueue<NetPkt> pkts) throws IOException {
        if(pkts != null) this.pkts = pkts;
        else this.pkts = new LinkedBlockingQueue<>();
        if(event != null) this.event = event;
        else this.event = new EventEmitter();
        buff = ByteBuffer.allocate(4);
        buff.order(ByteOrder.BIG_ENDIAN);
        in = socket.getInputStream();
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
    // - close socket reader
    public void close() throws IOException {
        socket.shutdownInput();
        in.close();
        event.emit("close-read", "addr", addr);
    }
    
    
    // ReadAction ()
    // - read incoming packets
    private void readAction() throws IOException, InterruptedException {
        int read = 0;
        while(!socket.isClosed()) {
            try { read = in.read(buff.array()); }
            catch(IOException e) { continue; }
            if(read < 4) break;
            int size = buff.getInt(0);
            byte[] data = new byte[size];
            read = in.read(data);
            if(read < size) break;
            NetPkt pkt = new NetPkt(addr, data);
            pkts.put(pkt);
            event.emit("read", "pkt", pkt);
        }
    }
    
    
    // Run ()
    // - read packets from socket
    @Override
    public void run() {
        try { readAction(); }
        catch(IOException | InterruptedException e) {}
        event.emit("disconnect", "addr", addr);
    }
}
