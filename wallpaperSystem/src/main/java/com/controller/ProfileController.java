package com.controller;

import cn.hutool.core.bean.BeanUtil;
import com.AppConstants;
import com.aop.LimitRequest;
import com.dto.PhotoDTO;
import com.echartModel.CategoryRatioModel;
import com.entity.Category;
import com.entity.ProfileCategory;
import com.entity.ProfilePhoto;
import com.entity.WallPhoto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.service.*;
import com.util.DataInter;
import com.util.MyData;
import com.util.PublicInterface;
import com.util.qiniu.QiNiuCloudUtil;
import com.util.redis.RedisConfig;
import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
public class ProfileController {
    @Autowired
    private ProfileCategoryService profileCategoryService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    private QiNiuCloudUtil util;
    @Autowired
    private AppConstants appConstants;
    @Value("${qiniu.path}")
    private String path;

    @GetMapping("/profile/profile_index")
    public ModelAndView toIndex(ModelAndView mv){
        mv.setViewName("profile/profile_index");
        return mv;
    }

    //上传网络图片
    @PostMapping("/profile/addonline")
    @ResponseBody
    public DataInter addWallpaper(ProfilePhoto profilePhoto){
        DataInter res = new DataInter();
        boolean flag = profileService.addonline(profilePhoto);
        if (flag){
            //清理缓存
            redisCacheService.clearProfilePhotoCache(profilePhoto.getCid());
            res.setMsg("添加成功~");
        }else {
            res.setCode(1);
            res.setMsg("添加失败，图片链接可能重复！");
        }
        return res;
    }

    @PostMapping("/profile/uploadPictures")
    @ResponseBody
    public PublicInterface<?> uploadPictures(@RequestParam("file") MultipartFile[] files, @RequestParam("cid")Integer cid) {
        PublicInterface<?> res = new PublicInterface<>();
        List<ProfilePhoto> list = new ArrayList<>();
        String systemPath = appConstants.getPath();
        try {
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    try {
                        String FILENAME_PREFIX = systemPath +new Date().getTime();
                        File file = new File(FILENAME_PREFIX+".jpg");
                        if (!file.getParentFile().exists()) file.getParentFile().mkdir();
                        //压缩图片
                        Thumbnails.of(files[i].getInputStream()).scale(1).outputQuality(0.7f).outputFormat("jpg").toFile(file);
                        FileInputStream fileInputStream1 = new FileInputStream(file);
                        String key = util.uploadQiniuYun(fileInputStream1);
                        String url = "http://"+path+"/"+key;
                        ProfilePhoto profilePhoto = new ProfilePhoto();
                        profilePhoto.setCid(cid);
                        profilePhoto.setPic(url);
                        profilePhoto.setFilekey(key);
                        list.add(profilePhoto);
                        //输出url上传后的,可以复制url到浏览器访问
                        System.out.println("url=" + url);
                        try {
                            //延迟100毫秒让七牛云缓一下
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //调用业务层，把图片链接加入数据库
            profileService.add(list);
            //清理缓存
            redisCacheService.clearProfilePhotoCache(cid);
            res.setMsg("上传成功");
        }catch (Exception e){
            e.printStackTrace();
            res.setMsg("上传失败");
            res.setCode(1);
        }
        //上传图片成功
        return res;
    }

    //删除头像图片
    @RequestMapping(value = "/profile/delete",method = RequestMethod.DELETE)
    @ResponseBody
    public DataInter delete(@RequestParam("id")Integer id,@RequestParam("filekey")String fileKey,@RequestParam("cid")Integer cid){
        boolean flag = profileService.delete(id,fileKey);
        DataInter dataInter = new DataInter();
        if (flag){
            //清除缓存
            redisCacheService.clearProfilePhotoCache(cid);
            dataInter.setMsg("删除成功！");
        }else {
            dataInter.setMsg("删除失败！");
        }
        return dataInter;
    }

    //跳转页面
    @GetMapping("/profile/profile_list")
    public ModelAndView getByCid(Integer cid,ModelAndView mv){
        mv.addObject("cid",cid);
        mv.setViewName("profile/profile_list");
        return mv;
    }

    //后台获取指定分类的壁纸
    @GetMapping("/profile/getProList")
    @ResponseBody
    public PublicInterface<ProfilePhoto> getProLlist(Integer cid,@RequestParam(value = "page",required = false,
            defaultValue = "1") Integer page, @RequestParam(value = "limit",required = false) Integer limit){
        //开启分页
        PageHelper.startPage(page,limit);
        PublicInterface<ProfilePhoto> res = new PublicInterface<>();
        List<ProfilePhoto> list = profileService.findPhotoByCid(cid);
        MyData<ProfilePhoto> data = new MyData<>();
        data.setList(list);
        res.setData(data);
        //封装分页数据
        PageInfo<ProfilePhoto> pageInfo = new PageInfo<>(list,limit);
        //封装记录条数
        res.setTotal(((Long)pageInfo.getTotal()).intValue());
        return res;
    }


    /*
        微信小程序获取壁纸接口
     */
    @LimitRequest
    @GetMapping("/profile/profile_list/{cid}/{start}/{end}")
    @ResponseBody
    public PublicInterface<PhotoDTO> toList(@PathVariable("cid")Integer cid, @PathVariable("start")Integer start, @PathVariable("end")Integer end, Model model){
        //先判断缓存有无信息
        List<PhotoDTO> datalist = redisCacheService.getProfilePhotoCache(cid, start, end);
        //没有缓存就从数据库中获取
        if (datalist == null){
            datalist = new ArrayList<>();
            int pageSize = end - start + 1 >= 100 ? 100 : end - start + 1;
            //开启分页
            PageHelper.startPage((start/pageSize)+1,pageSize);
            //获取指定的分类信息
            List<ProfileCategory> categoryList = profileCategoryService.findByCid(cid);
            //如果该分类没有被隐藏
            if (categoryList.size() != 0){
                //获取图片列表
                List<ProfilePhoto> photoList = profileService.findPhotoByCid(cid);
                for (ProfilePhoto profilePhoto : photoList){
                    //获取到的数据转换成DTO对象
                    PhotoDTO photoDTO = new PhotoDTO();
                    BeanUtil.copyProperties(profilePhoto,photoDTO);
                    datalist.add(photoDTO);
                }
            }
            //更新缓存
            redisCacheService.clearProfilePhotoCache(cid);
            redisCacheService.addProfilePhotoCache(cid);
        }

        //封装数据
        PublicInterface<PhotoDTO> res = new PublicInterface<>();
        MyData<PhotoDTO> data = new MyData<>();
        data.setList(datalist);
        data.setNextStart(start+18);
        res.setMsg("success");
        res.setData(data);
        return res;

    }

    @GetMapping("/profile/profile_upload")
    public ModelAndView toUpload(Model model,ModelAndView mv){
        List<ProfileCategory> list = profileCategoryService.getAll();
        mv.setViewName("profile/profile_upload");
        mv.addObject("categorys",list);
        return mv;
    }

    //统计壁纸图片个数(Echarts)
    @GetMapping("/profile/getTotal")
    @ResponseBody
    public DataInter<Integer> getTotal(){
        int amount = profileService.countTotal();
        DataInter<Integer> res = new DataInter<>();
        res.setData(amount);
        res.setTotal(1);
        return res;
    }

    //计算头像各分类的图片数量（Echarts)
    @GetMapping("/profile/getRatio")
    @ResponseBody
    public DataInter<List> getRatio(){
        //获取壁纸的所有分类信息
        List<ProfileCategory> categoryList = profileCategoryService.getList();
        DataInter<List> res = new DataInter<>();
        List<CategoryRatioModel> categoryRatioModelList = new ArrayList<>();
        //按照分类ID得出该分类的图片数
        for (ProfileCategory category : categoryList){
            int amount = profileService.countForCount(category.getCid());
            categoryRatioModelList.add(new CategoryRatioModel(amount,category.getTitle()));
        }
        res.setTotal(categoryRatioModelList.size());
        res.setData(categoryRatioModelList);
        return res;
    }


}
