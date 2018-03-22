package com.sohu.metaq.web.utils;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class Http3xUtil {

    private final static Logger logger = LoggerFactory.getLogger(Http3xUtil.class);

    public static String getMethod(String url) {


        String str = "network is error.";
        // 构造HttpClient的实例
        HttpClient httpClient = new HttpClient();
        // 创建GET方法的实例
        GetMethod getMethod = new GetMethod(url);
        // 使用系统提供的默认的恢复策略
        getMethod.getParams().setSoTimeout(1000);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        try {
            // 执行getMethod
            int statusCode = httpClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + getMethod.getStatusLine());
            }
            // 处理内容
            str = getMethod.getResponseBodyAsString();
        } catch (Exception e) {
            // 发生网络异常
            logger.error("Http3xUtil getMethod is error.",e);
        } finally {
            // 释放连接
            getMethod.releaseConnection();
        }
        return str;
    }

    public static String getMethodWithPara(String url ,List<NameValuePair> nameValuePairs, String paramCharSet){
        if(nameValuePairs == null || nameValuePairs.size() == 0){
            return getMethod(url);
        }
        String params = EncodingUtil.formUrlEncode(nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]), paramCharSet);
        StringBuilder newUrl = new StringBuilder(url);
        newUrl.append("?").append(params);
        return getMethod(newUrl.toString());
    }

    public static String postMethod(String url) {


        String str = "network is error.";
        // 构造HttpClient的实例
        HttpClient httpClient = new HttpClient();
        // 创建GET方法的实例
        PostMethod postMethod = new PostMethod(url);
        // 使用系统提供的默认的恢复策略
        postMethod.getParams().setSoTimeout(1000);
        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        try {
            // 执行getMethod
            int statusCode = httpClient.executeMethod(postMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + postMethod.getStatusLine());
            }
            // 处理内容
            InputStream inputStream = postMethod.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer resBuffer = new StringBuffer();
            String resTemp= "";
            while((resTemp = br.readLine()) != null){
                resBuffer .append(resTemp);
            }
            str = resBuffer.toString();
        } catch (Exception e) {
            logger.error("Http3xUtil getMethod is error.",e);
        } finally {
            postMethod.releaseConnection();
        }
        return str;
    }


}