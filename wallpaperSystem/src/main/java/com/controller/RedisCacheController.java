package com.controller;

import com.service.CategoryService;
import com.service.ProfileCategoryService;
import com.service.RedisCacheService;
import com.util.DataInter;
import com.util.redis.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.crypto.Data;
import java.util.Set;

@RestController
public class RedisCacheController {

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProfileCategoryService profileCategoryService;

    //清除全局缓存
    @DeleteMapping("/flushdb")
    public DataInter flushdb(){
        DataInter res = new DataInter();
        if (redisCacheService.clearGlobalCache()){
            res.setMsg("清除成功！");
        }else {
            res.setMsg("清除失败！");
        }
        return res;
    }

    //清除壁纸图片的所有缓存
    @DeleteMapping("/clearWallpaperCache")
    public DataInter clearWallpaperCache(){
        DataInter res = new DataInter();
        boolean flag = redisCacheService.clearAllWallpaperCache();
        if (flag){
            res.setMsg("清除成功！");
        }else {
            res.setMsg("清除失败");
        }
        return res;
    }

    //清除头像图片的所有缓存
    @DeleteMapping("/clearProfileCache")
    public DataInter clearProfileCache(){
        DataInter res = new DataInter();
        boolean flag = redisCacheService.clearAllProfilePhotoCache();
        if (flag){
            res.setMsg("清除成功！");
        }else {
            res.setMsg("清除失败");
        }
        return res;
    }

    //更新壁纸分类缓存
    @PostMapping("/updateWallCategoryCache")
    public DataInter clearWallcategory(){
        DataInter res = new DataInter();
        if (redisCacheService.updateWallCategoryCache()){
            res.setMsg("更新成功！");
        }else {
            res.setMsg("更新失败，请稍后再试");
        }
        return res;
    }


    //更新头像分类缓存
    @PostMapping("/updateProfileCategoryCache")
    public DataInter clearProfileCategory(){
        DataInter res = new DataInter();
        if (redisCacheService.updateProfileCategoryCache()){
            res.setMsg("更新成功！");
        }else {
            res.setMsg("更新失败，请稍后再试");
        }
        return res;
    }

    //更新头像图片缓存
    @PostMapping("/updateProfilePhotoCache")
    public DataInter updateProfilePhotoCache(Integer cid){
        DataInter res = new DataInter();
        redisCacheService.clearProfilePhotoCache(cid);
        if(profileCategoryService.findByCid(cid).size() != 0){
            if (redisCacheService.addProfilePhotoCache(cid)){
                res.setMsg("更新成功！");
            }else {
                res.setMsg("更新失败，请稍后再试");
            }
        }else {
            res.setMsg("该分类已隐藏，无需更新！");
        }
        return res;
    }

    //更新壁纸图片缓存
    @PostMapping("/updateWallPhotoCache")
    public DataInter updateWallPhotoCache(Integer cid){
        DataInter res = new DataInter();
        redisCacheService.clearWallpaperCache(cid);
        if (categoryService.findByCid(cid).size() != 0){
            if (redisCacheService.addWallpaperCache(cid)){
                res.setMsg("更新成功！");
            }else {
                res.setMsg("更新失败，请稍后再试");
            }
        }else {
            res.setMsg("该分类已隐藏，无需更新！");
        }

        return res;
    }

}
