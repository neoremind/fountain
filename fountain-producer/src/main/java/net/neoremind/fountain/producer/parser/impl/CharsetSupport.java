package net.neoremind.fountain.producer.parser.impl;

import java.util.HashMap;
import java.util.Map;

public class CharsetSupport {
    private CharsetSupport(){}
    private final static Map<String,String> charSetMap = new HashMap<String,String>();
    static{
        charSetMap.put("utf8", "utf-8");
    }
    public static String convertDbCharset2Java(String charSet){
        if(charSetMap.containsKey(charSet)){
            return charSetMap.get(charSet);
        }
        return charSet;
    }
}
