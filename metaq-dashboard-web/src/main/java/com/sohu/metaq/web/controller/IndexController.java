package com.sohu.metaq.web.controller;

import com.sohu.metaq.service.ResourcesHolder;
import com.taobao.metamorphosis.cluster.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * Created by xiaofeiliu on 2015/3/9.
 */
@Controller
public class IndexController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping(value = "index.do", method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        model.setViewName("index");
        return model;
    }

    @RequestMapping(value = "clusterU.do", method = RequestMethod.GET)
    public ModelAndView cluster4User(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        String zkUrlPre = request.getParameter("zkUrlPre");
        String groupPre = request.getParameter("groupPre");
        String topicPre = request.getParameter("topicPre");
        Set<String> zkUrls = ResourcesHolder.zkClientCache.keySet();
        model.addObject("zkUrls",zkUrls);
        model.addObject("zkUrlPre",zkUrlPre);
        model.addObject("groupPre",groupPre);
        model.addObject("topicPre",topicPre);
        model.setViewName("clusterU");
        return model;
    }

    @RequestMapping(value = "clusterM.do", method = RequestMethod.GET)
    public ModelAndView cluster4Manager(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Set<String> zkUrls = ResourcesHolder.zkClientCache.keySet();
        model.addObject("zkUrls",zkUrls);
        model.setViewName("clusterM");
        return model;
    }

    @RequestMapping(value = "clusterC.do", method = RequestMethod.GET)
    public ModelAndView cluster4InCharge(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Set<String> zkUrls = ResourcesHolder.zkClientCache.keySet();
        model.addObject("zkUrls",zkUrls);
        model.setViewName("clusterC");
        return model;
    }

    @RequestMapping(value = "clusterS.do", method = RequestMethod.GET)
    public ModelAndView cluster4Search(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Set<String> zkUrls = ResourcesHolder.zkClientCache.keySet();
        model.addObject("zkUrls",zkUrls);
        model.setViewName("clusterS");
        return model;
    }


}
