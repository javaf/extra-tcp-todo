// @wolfram77
package org.data;

// required modules
import java.util.*;



public class Coll {
    
    // Map (arr)
    // - converts an array of key, value pairs to map
    public static Map map(Object[] arr) {
        Map map = new HashMap();
        for(int i=0; i<arr.length; i+=2)
            map.put(arr[i], arr[i+1]);
        return map;
    }
    
    
    // add some more basic functionality here
}
