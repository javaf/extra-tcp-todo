// @wolfram77
package org.net;

// required modules
import java.util.concurrent.*;
import java.net.*;
import java.nio.*;
import java.io.*;
import org.event.*;



public class TcpClient extends Thread {
    
    // data
    InetSocketAddress addr;
    EventEmitter event;
    SocketWriter out;
    SocketReader in;
    Socket socket;
    
    
    // Init (event, socket, tx, rx)
    // - initialize tcp client
    private void init(EventEmitter event, Object link, BlockingQueue<NetPkt> tx, BlockingQueue<NetPkt> rx) throws IOException {
        buff = ByteBuffer.allocate(4);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        if(link instanceof Socket) socket = (Socket)link;
        else socket = Inet.connect((InetSocketAddress)link);
        this.addr = Inet.addr(socket);
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
        if(event != null) this.event = event;
        else this.event = new EventEmitter();
        if(rx != null) this.rx = rx;
        else this.rx = new LinkedBlockingQueue<>();
        this.tx = tx;
        start();
    }
    
    
    // TcpClient (event, socket, tx, rx)
    // - create tcp client with existing event emitter
    public TcpClient(EventEmitter event, Object link, BlockingQueue<NetPkt> tx, BlockingQueue<NetPkt> rx) throws IOException {
        init(event, link, tx, rx);
    }
    
    
    // TcpClient (socket, tx, rx)
    // - create tcp client from new event emitter
    public TcpClient(Object link, BlockingQueue<NetPkt> tx, BlockingQueue<NetPkt> rx) throws IOException {
        this(null, link, tx, rx);
    }
    
    
    // ReadAction ()
    // - read incoming packets
    private void readAction() throws IOException, InterruptedException {
        while(!socket.isClosed()) {
            in.read(buff.array());
            byte[] data = new byte[buff.getInt(0)];
            in.read(data);
            NetPkt pkt = new NetPkt(addr, data);
            rx.put(pkt);
            event.emit("read", "pkt", pkt);
        }
    }
    
    
    // Run ()
    // - receive data from client
    @Override
    public void run() {
        try { readAction(); }
        catch(IOException | InterruptedException e) { event.emit("read-error", "err", e, "socket", socket); }
    }
    
    
    // Read ()
    // - read data from client (blocking)
    public byte[] read() throws InterruptedException {
        return rx.take().data();
    }
    
    
    // Write (data)
    // - write data to client
    public TcpClient write(byte[] data, boolean now) throws InterruptedException, IOException {
        if(tx != null && !now) { tx.put(new NetPkt(addr, data)); return this; }
        buff.putInt(0, data.length);
        out.write(buff.array());
        out.write(data);
        event.emit("write", "pkt", new NetPkt(addr, data));
        return this;
    }
    
    
    // Write (data)
    // - write data to client
    public TcpClient write(byte[] data) throws InterruptedException, IOException {
        return write(data, false);
    }
    
    
    // Event ()
    // - get event emitter
    public EventEmitter event() {
        return event;
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
        socket.shutdownInput();
        socket.shutdownOutput();
        out.close();
        in.close();
        socket.close();
    }
}
