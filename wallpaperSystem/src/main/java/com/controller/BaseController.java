package com.controller;

import com.aop.LimitRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/*
    负责路径跳转
 */
@Controller
public class BaseController {
    @LimitRequest
    @RequestMapping("/")
    public ModelAndView index(ModelAndView modelAndView){
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @RequestMapping("/wallpaper/category_list")
    public ModelAndView toCategory(ModelAndView mv) {
        mv.setViewName("wallpaper/category_list");
        return mv;
    }

    @RequestMapping("/help/help")
    public ModelAndView toHelp(ModelAndView mv) {
        mv.setViewName("help/help");
        return mv;
    }


    @RequestMapping("/home")
    public ModelAndView home(ModelAndView modelAndView) {
        modelAndView.setViewName("home");
        return modelAndView;
    }

    @RequestMapping("/login")
    public ModelAndView login(ModelAndView mv) {
        mv.setViewName("login");
        return mv;
    }

    @RequestMapping("/profile/category_list")
    public ModelAndView toProfileCategory(ModelAndView mv){
        mv.setViewName("profile/category_list");
        return mv;
    }

    @RequestMapping("/feedback/list")
    public ModelAndView toFeedbackList(ModelAndView mv){
        mv.setViewName("feedback/list");
        return mv;
    }


}
