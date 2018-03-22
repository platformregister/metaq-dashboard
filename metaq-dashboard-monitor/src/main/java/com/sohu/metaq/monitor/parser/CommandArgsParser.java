package com.sohu.metaq.monitor.parser;

import java.util.HashMap;

/**
 * Created by xiaofeiliu on 2014/8/13.
 */
public class CommandArgsParser {

    public static HashMap<String, String> parse(String[] args) throws Exception {
        HashMap<String, String> argsHashMap = new HashMap();
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg.indexOf("-")>= 0 && arg.indexOf("=") > 0) {
                    arg = arg.substring(arg.indexOf("-")+1, arg.length());
                    String[] subagrs = arg.split("\\=");
                    argsHashMap.put(subagrs[0], subagrs[1]);
                } else {
                    throw new Exception("Invalid Args!");
                }
            }
        }
        return argsHashMap;
    }

}
