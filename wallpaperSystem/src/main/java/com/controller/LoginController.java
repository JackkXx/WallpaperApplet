package com.controller;

import com.entity.User;
import com.service.UserSerivce;
import com.util.DataInter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
@Controller
public class LoginController {

    @Autowired
    private UserSerivce userSerivce;
    @RequestMapping("/login/check")
    @ResponseBody
    public DataInter loginCheck(@RequestBody Map<String,Object> map, HttpSession session){
        DataInter<?> info = new DataInter<>();
        Object username = map.get("username");
        Object password = map.get("password");
        User user = userSerivce.findByUserName(username.toString());
        info.setStatus(true);
        if (user == null){
            info.setMsg("用户不存在，请联系管理员");
        } else if (user.getPassword().equals(password.toString())){
            info.setMsg("登录成功");
            session.setAttribute("user",username.toString());
            info.setPath("/home");
            Map<String,String> userMap = new HashMap<>();
            userMap.put("username",map.get("username").toString());
        }else {
            info.setMsg("密码错误");
        }
        return info;

    }

    /*
        注销
     */
    @RequestMapping("/logout")
    public ModelAndView logout(HttpSession session,ModelAndView mv){
        session.removeAttribute("user");
        mv.setViewName("login");
        return mv;
    }
}
