package com.sohu.metaq.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaofeiliu on 2014/7/3.
 */
public class JsonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);
    public static final String CharacterEncoding = "utf-8";

    public static <T>  T readValue(byte[] src, Class<T> cls) throws Exception{
        try {
            return (T)JSON.parseObject(new String(src, CharacterEncoding), cls);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    public static String writeValue(Object entity, boolean prettyFormat) throws Exception{
        try {
            return JSON.toJSONString(entity, prettyFormat);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    public static String map2Json(Map<String, String> map) {
        try {
            if (map.isEmpty())
                return "{}";
            return JSON.toJSONString(map, false);
        } catch (Exception e) {
            LOG.error("map2Json is error.",e);
        }
        return null;
    }

    public static boolean isValidateJsonStr(String jsonStr) {
        try {
            JSON.parseObject(jsonStr);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public static Map<String, Object> json2Map(String jsonStr){
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map = (Map<String, Object>) JSON.parse(jsonStr);
            return map;
        } catch (Exception e) {
            LOG.error("json2Map is error.",e);
        }
        return null;
    }

    public static JSONObject parseJson(String jsonStr) {
        try {
            return JSON.parseObject(jsonStr);
        } catch (Exception e) {
            LOG.error("parseJson is error.",e);
            return null;
        }
    }


}
