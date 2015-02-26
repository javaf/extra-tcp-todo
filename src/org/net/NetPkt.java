// @wolfram77
package org.net;

// required modules
import java.net.*;



public class NetPkt {
    
    // data
    InetSocketAddress addr;
    byte[] data;
    
    
    // TcpPkt (addr, data)
    // - create a tcp packet
    public NetPkt(InetSocketAddress addr, byte[] data) {
        this.addr = addr;
        this.data = data;
    }
    
    
    // Addr ()
    // - get packet address
    public InetSocketAddress addr() {
        return addr;
    }
    
    
    // Data ()
    // - get packet data
    public byte[] data() {
        return data;
    }
}
