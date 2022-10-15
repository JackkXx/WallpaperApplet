package com.controller;

import com.entity.Category;
import com.entity.Feedback;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.service.FeedbackService;
import com.util.MyData;
import com.util.PublicInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;

    //后台分页获取反馈信息
    @GetMapping("/getList")
    public PublicInterface<Feedback> getList(@RequestParam(value = "page",required = false,defaultValue = "1") Integer page,
                                            @RequestParam(value = "limit",required = false) Integer limit){
        PageHelper.startPage(page,limit);
        PublicInterface<Feedback> res = new PublicInterface<>();
        List<Feedback> dataList = feedbackService.findAll();
        //封装分页数据
        PageInfo<Feedback> pageInfo = new PageInfo<>(dataList,10);
        res.setData(new MyData<Feedback>(dataList,0));
        res.setMsg("success");
        res.setTotal(((Long)pageInfo.getTotal()).intValue());
        return res;
    }

    //删除分类
    @DeleteMapping("/delete")
    public PublicInterface<Feedback> delete(Integer id){
        PublicInterface<Feedback> res = new PublicInterface<>();
        if (feedbackService.delete(id)){
            res.setCode(0);
            res.setMsg("删除成功！");
        }else {
            res.setCode(1);
            res.setMsg("删除失败！");
        }
        return res;
    }

    @PostMapping("/add")
    public PublicInterface<Feedback> add(@RequestBody Feedback feedback){
        PublicInterface<Feedback> res = new PublicInterface<>();
        boolean flag = feedbackService.add(feedback);
        if (flag){
            res.setMsg("反馈成功！");
        }else {
            res.setCode(1);
            res.setMsg("反馈失败，请稍后再试");
        }
        return res;
    }


}
