// @wolfram77
package org.net;

// required modules
import java.util.concurrent.*;
import java.net.*;
import java.nio.*;
import java.io.*;
import org.event.*;



public class TcpClient extends Thread {

    // constants
    final int timeout = 10000;

    // errors
    final String eNoConnection = "Connection not Established";
    
    // data
    ByteBuffer buff;
    EventEmitter event;
    BlockingQueue<TcpPkt> tx;
    BlockingQueue<TcpPkt> rx;
    InetSocketAddress addr;
    OutputStream out;
    InputStream in;
    Socket socket;
    
    
    // Init (event, socket, tx, rx)
    // - initialize tcp client
    private void init(EventEmitter event, Socket socket, BlockingQueue<TcpPkt> tx, BlockingQueue<TcpPkt> rx) throws IOException {
        buff = ByteBuffer.allocate(4);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        this.addr = Inet.addr(socket);
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
        this.socket = socket;
        if(event != null) this.event = event;
        else this.event = new EventEmitter();
        if(rx != null) this.rx = rx;
        else this.rx = new LinkedBlockingQueue<>();
        this.tx = tx;
        start();
    }
    
    
    // TcpClient (event, socket, tx, rx)
    // - create tcp client from existing socket
    public TcpClient(EventEmitter event, Socket socket, BlockingQueue<TcpPkt> tx, BlockingQueue<TcpPkt> rx) throws IOException {
        init(event, socket, tx, rx);
    }
    
    
    // TcpClient (socket, tx, rx)
    // - create tcp client from existing socket
    public TcpClient(Socket socket, BlockingQueue<TcpPkt> tx, BlockingQueue<TcpPkt> rx) throws IOException {
        this(null, socket, tx, rx);
    }
    
    
    // TcpClient (event, addr, tx, rx)
    // - connect to a new tcp client
    public TcpClient(EventEmitter event, InetSocketAddress addr, BlockingQueue<TcpPkt> tx, BlockingQueue<TcpPkt> rx) throws IOException  {
        socket = new Socket();
        socket.setSoTimeout(timeout);
        socket.connect(addr);
        if(!socket.isConnected())
            throw new SocketException(eNoConnection);
        init(event, socket, tx, rx);
    }
    
    
    // TcpClient (addr, tx, rx)
    // - connect to a new tcp client
    public TcpClient(InetSocketAddress addr, BlockingQueue<TcpPkt> tx, BlockingQueue<TcpPkt> rx) throws IOException  {
        this(null, addr, tx, rx);
    }
    
    
    // Run ()
    // - receive data from client
    @Override
    public void run() {
        try {
            while(!socket.isClosed()) {
                in.read(buff.array());
                byte[] data = new byte[buff.getInt(0)];
                in.read(data);
                TcpPkt pkt = new TcpPkt(addr, data);
                rx.put(pkt);
                event.emit("read", "pkt", pkt);
            }
        }
        catch(IOException | InterruptedException e) { event.emit("read-error", "err", e, "socket", socket); }
    }
    
    
    // Read ()
    // - read data from client (blocking)
    public byte[] read() throws InterruptedException {
        return rx.take().data();
    }
    
    
    // Write (data)
    // - write data to client
    public TcpClient write(byte[] data) throws InterruptedException, IOException {
        if(tx != null) { tx.put(new TcpPkt(addr, data)); return this; }
        buff.putInt(0, data.length);
        out.write(buff.array());
        out.write(data);
        return this;
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
        socket.close();
        out.close();
        in.close();
    }
}
