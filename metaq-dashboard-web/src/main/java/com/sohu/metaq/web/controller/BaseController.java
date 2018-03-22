package com.sohu.metaq.web.controller;

import com.sohu.metaq.web.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by xiaofeiliu on 2014/6/17.
 */
@Controller
public class BaseController {

    private final static Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected void write(HttpServletResponse response ,Map map) {
        Writer writer = null;
        try {
            writer = response.getWriter();
            writer.write(JsonUtils.map2Json(map));
            writer.flush();
        } catch (Exception e) {
            logger.error("response.getWriter() is error.",e);
        } finally {
            if(null != writer) {
                try {
                    writer.close();
                } catch (Exception e) {
                    logger.error("writer's close is error.",e);
                }
            }
        }

    }

    protected void write(HttpServletResponse response ,String str) {
        Writer writer = null;
        try {
            writer = response.getWriter();
            writer.write(str);
            writer.flush();
        } catch (Exception e) {
            logger.error("response.getWriter() is error.",e);
        } finally {
            if(null != writer) {
                try {
                    writer.close();
                } catch (Exception e) {
                    logger.error("writer's close is error.",e);
                }
            }
        }

    }

    protected boolean verify(String userName, String password) {
        if(userName == null || userName == "" || password == null || password == "") {
            return false;
        }
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://ldap.sohu-inc.com");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userName + "@sohu-inc.com");
        env.put(Context.SECURITY_CREDENTIALS, password);
        DirContext ctx = null;
        try {
            // 用户密码在域控中验证，如果验证失败则会抛出异常
            ctx = new InitialDirContext(env);
            return true;
        } catch (Exception e) {
            logger.error("MonitorController's verify is error.",e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


}
