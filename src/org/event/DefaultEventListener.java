// @wolfram77
package org.event;

// required modules
import java.util.*;



public class DefaultEventListener implements IEventListener {

    // Listen (event, args)
    // - print event details and exit on error
    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void listen(String event, Map args) {
        System.out.println("["+event+"] : "+args);
        if(!args.containsKey("err")) return;
        Throwable err = (Throwable)args.get("err");
        err.printStackTrace();
        System.exit(-1);
    }
}
