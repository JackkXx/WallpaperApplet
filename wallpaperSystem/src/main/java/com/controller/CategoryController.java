package com.controller;

import com.aop.LimitRequest;
import com.entity.Category;
import com.entity.ProfileCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.service.CategoryService;
import com.service.RedisCacheService;
import com.service.WallpaperService;
import com.util.DataInter;
import com.util.MyData;
import com.util.PublicInterface;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private WallpaperService wallpaperService;
    @Autowired
    private RedisCacheService redisCacheService;


    /*
        小程序前台获取所有分类信息(不包含隐藏的)
     */
    @LimitRequest
    @GetMapping("/getAll")
    public PublicInterface<Category> getAll(){
        PublicInterface<Category> res = new PublicInterface<>();
        List<Category> dataList = null;
        try {
            dataList = categoryService.getAll();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        res.setData(new MyData<Category>(dataList,0));
        res.setMsg("success");
        res.setTotal(dataList.size());
        return res;
    }

    @GetMapping("/getList")
    public PublicInterface<Category> getList(){
        PublicInterface<Category> res = new PublicInterface<>();
        List<Category> dataList = null;
        dataList = categoryService.getList();
        res.setData(new MyData<Category>(dataList,0));
        res.setMsg("success");
        res.setTotal(dataList.size());
        return res;
    }

    //后台分页获取分类信息（包含隐藏的）
    @GetMapping("/getListByPage")
    public PublicInterface<Category> getListByPage(@RequestParam(value = "page",required = false,defaultValue = "1") Integer page,
                                            @RequestParam(value = "limit",required = false) Integer limit){
        PageHelper.startPage(page,limit);
        PublicInterface<Category> res = new PublicInterface<>();
        List<Category> dataList = categoryService.getList();
        //封装分页数据
        PageInfo<Category> pageInfo = new PageInfo<>(dataList,10);
        res.setData(new MyData<Category>(dataList,0));
        res.setMsg("success");
        res.setTotal(((Long)pageInfo.getTotal()).intValue());
        return res;
    }

    //显示与隐藏
    @PostMapping("/updateShow")
    public PublicInterface<Category> alterShow(Category category){
        PublicInterface<Category> res = new PublicInterface<>();
        boolean flag = categoryService.updateShow(category);
        if (flag){
            //清除该分类下的所有缓存
            redisCacheService.clearWallpaperCache(category.getCid());
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
    public PublicInterface edit(Category category){
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
    public PublicInterface<Category> add(Category category){
        PublicInterface<Category> res = new PublicInterface<>();
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
    public PublicInterface<Category> delete(Integer cid){
        PublicInterface<Category> res = new PublicInterface<>();
        int row = wallpaperService.countForCount(cid);
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
