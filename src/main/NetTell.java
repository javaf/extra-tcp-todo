// @wolfram77
package main;

// required modules
import java.util.*;
import org.event.*;
import org.net.*;



public class NetTell implements IEventListener {
    
    @Override
    public void listen(String event, Map args) {
        byte[] data = ((NetPkt)args.get("pkt")).data();
        System.out.println("["+event+"] : data="+Arrays.toString(data));
    }
}
