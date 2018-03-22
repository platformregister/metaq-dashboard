package com.sohu.metaq.web.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by xiaofeiliu on 2014/6/11.
 */
public class EncodingFilter implements Filter {
    protected FilterConfig filterConfig = null;
    protected String encoding = "";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.encoding = filterConfig.getInitParameter("encoding");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        request.setCharacterEncoding(this.encoding);
        response.setCharacterEncoding(this.encoding);
        response.setContentType("text/html;charset=" + this.encoding);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        this.encoding = null;
        this.filterConfig = null;
    }

}
