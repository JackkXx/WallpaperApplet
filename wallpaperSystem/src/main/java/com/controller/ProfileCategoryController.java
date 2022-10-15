package com.controller;

import com.aop.LimitRequest;
import com.entity.Category;
import com.entity.ProfileCategory;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.service.*;
import com.util.MyData;
import com.util.PublicInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profileCategory")
public class ProfileCategoryController {
    @Autowired
    private ProfileCategoryService categoryService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private RedisCacheService redisCacheService;


    /*
        微信小程序获取所有分类信息
     */
    @LimitRequest
    @RequestMapping("/getAll")
    public PublicInterface<ProfileCategory> getAll(){
        PublicInterface<ProfileCategory> res = new PublicInterface<>();
        List<ProfileCategory> dataList = categoryService.getAll();
        res.setData(new MyData<ProfileCategory>(dataList,0));
        res.setMsg("success");
        res.setTotal(dataList.size());
        return res;
    }

    /*
       后台获取所有分类信息(包含隐藏的）
    */
    @RequestMapping("/getList")
    public PublicInterface<ProfileCategory> getAlls(){
        PublicInterface<ProfileCategory> res = new PublicInterface<>();
        List<ProfileCategory> dataList = categoryService.getList();
        res.setData(new MyData<ProfileCategory>(dataList,0));
        res.setMsg("success");
        res.setTotal(dataList.size());
        return res;
    }

    /*
        后台分页获取分类信息
     */
    @RequestMapping("/getListByPage")
    public PublicInterface<ProfileCategory> getList(@RequestParam(value = "page",required = false,defaultValue = "1") Integer page,
                                                    @RequestParam(value = "limit",required = false) Integer limit){
        PageHelper.startPage(page,limit);
        PublicInterface<ProfileCategory> res = new PublicInterface<>();
        List<ProfileCategory> dataList = categoryService.getList();
        //封装分页数据
        PageInfo<ProfileCategory> pageInfo = new PageInfo<>(dataList,10);
        res.setData(new MyData<ProfileCategory>(dataList,0));
        res.setMsg("success");
        res.setTotal(((Long)pageInfo.getTotal()).intValue());
        return res;
    }

    //显示与隐藏
    @PostMapping("/updateShow")
    public PublicInterface<ProfileCategory> alterShow(ProfileCategory category){
        PublicInterface<ProfileCategory> res = new PublicInterface<>();
        boolean flag = categoryService.updateShow(category);
        if (flag){
            redisCacheService.clearProfilePhotoCache(category.getCid());
            if (category.getIs_show() == 1 ){
                res.setMsg("显示成功");
            }else {
                res.setMsg("隐藏成功");
            }

        }else {
            res.setMsg("操作失败，请稍后再试！");
        }
        return res;
    }

    @PutMapping("/edit")
    public PublicInterface edit(ProfileCategory category){
        PublicInterface res = new PublicInterface();
        boolean flag = categoryService.edit(category);
        if (flag){
            res.setMsg("修改成功！");
        }else {
            res.setMsg("修改失败，请稍后再试");
        }
        return res;
    }

    /*
        添加分类
     */
    @RequestMapping("/add")
    public PublicInterface<ProfileCategory> add(ProfileCategory category){
        PublicInterface<ProfileCategory> res = new PublicInterface<>();
        boolean flag = categoryService.add(category);
        if (flag){
            res.setMsg("添加成功");
        }else {
            res.setCode(1);
            res.setMsg("添加失败");
        }
        return res;
    }

    //删除分类
    @RequestMapping("/delete")
    public PublicInterface<ProfileCategory> delete(Integer cid){
        PublicInterface<ProfileCategory> res = new PublicInterface<>();
        int row = profileService.countForCount(cid);
        if (row > 0){
            res.setMsg("请先清空该分类下的壁纸,再删除");
            res.setCode(1);
            return res;
        }
        if (cid != null){
            if (categoryService.delete(cid)){
                res.setMsg("删除成功");
            }else {
                res.setMsg("删除失败");
                res.setCode(1);
            }

        }
        return res;
    }


}
