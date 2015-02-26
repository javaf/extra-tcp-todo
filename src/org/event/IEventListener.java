// @wolfram77
package org.event;

// required modules
import java.util.*;



public interface IEventListener {
    
    // Listen (event, args)
    // - listen to an event, args provide additional info
    void listen(String event, Map args);
}
