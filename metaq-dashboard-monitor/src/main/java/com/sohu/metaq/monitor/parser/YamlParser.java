package com.sohu.metaq.monitor.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by xiaofeiliu on 2014/8/13.
 */
public class YamlParser {

    public static HashMap<String, String> parse(String filePath) throws IOException {
        HashMap<String, String> parameters = new HashMap<String, String>();
        File file = new File(filePath);
        if (file.exists()) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String lineStr = null;
            while ((lineStr = bufferedReader.readLine()) != null) {
                if (lineStr.indexOf("#") != 0 && lineStr.indexOf("=") > 0) {
                    String[] parameter = lineStr.split("\\=");
                    if(parameter.length==2) {
                        parameters.put(parameter[0], parameter[1]);
                    } else if (parameter.length > 2 ) {
                        String p1 = lineStr.substring(0,lineStr.indexOf('='));
                        String p2 = lineStr.substring(lineStr.indexOf('=')+1);
                        parameters.put(p1, p2);
                    }
                }
            }
        }
        return parameters;
    }


}
