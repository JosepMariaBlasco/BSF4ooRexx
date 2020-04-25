package org.oorexx.doc.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class IdGenerator {

    private static ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    public static synchronized String getId(String name){
        String encoded = name;
        try {
            encoded = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if(!map.containsKey(encoded)){
            map.put(encoded, 1);
            return encoded;
        } else {
            Integer integer = map.get(encoded);
            map.put(encoded, integer+1);
            return encoded + "-" + integer;
        }
    }

    public static synchronized void reset(){
        map = new ConcurrentHashMap<>();
    }

}
