package com.controller;

import cn.hutool.core.bean.BeanUtil;
import com.AppConstants;
import com.aop.LimitRequest;
import com.dto.PhotoDTO;
import com.echartModel.CategoryRatioModel;
import com.entity.Category;
import com.entity.WallPhoto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.service.CategoryService;
import com.service.RedisCacheService;
import com.service.WallpaperService;
import com.util.PublicInterface;
import com.util.DataInter;
import com.util.MyData;
import com.util.qiniu.QiNiuCloudUtil;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class WallpaperController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private WallpaperService wallpaperService;
    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    private QiNiuCloudUtil util;
    @Autowired
    private AppConstants appConstants;
    @Value("${qiniu.path}")
    private String path;

    @GetMapping("/wallpaper/wall_index")
    public ModelAndView toIndex(ModelAndView mv){
        mv.setViewName("wallpaper/wall_index");
        return mv;
    }

    //上传网络图片
    @PostMapping("/wallpaper/addonline")
    @ResponseBody
    public DataInter addWallpaper(WallPhoto wallPhoto){
        DataInter res = new DataInter();
        boolean flag = wallpaperService.addonline(wallPhoto);
        if (flag){
            res.setMsg("添加成功~");
        }else {
            res.setCode(1);
            res.setMsg("添加失败，图片链接可能重复！");
        }
        return res;
    }

    @PostMapping("/uploadPictures")
    @ResponseBody
    public PublicInterface<?> uploadPictures(@RequestParam("file") MultipartFile[] files, @RequestParam("cid")Integer cid) {
        PublicInterface<?> res = new PublicInterface<>();
        List<WallPhoto> list = new ArrayList<>();
        try {
            String systemPath = appConstants.getPath();
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
                        WallPhoto wallPhoto = new WallPhoto();
                        wallPhoto.setCid(cid);
                        wallPhoto.setPic(url);
                        wallPhoto.setFilekey(key);
                        list.add(wallPhoto);
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
            wallpaperService.add(list);
            //清理缓存
            redisCacheService.clearWallpaperCache(cid);
            res.setMsg("上传成功");
        }catch (Exception e){
            e.printStackTrace();
            res.setMsg("上传失败");
            res.setCode(1);
        }
        //上传图片成功
        return res;
    }

    //删除
    @RequestMapping(value = "/wallpaper/delete",method = RequestMethod.DELETE)
    @ResponseBody
    public DataInter delete(@RequestParam("id")Integer id,@RequestParam("filekey")String fileKey,@RequestParam("cid")Integer cid){
        boolean flag = wallpaperService.delete(id,fileKey);
        DataInter dataInter = new DataInter();
        if (flag){
            //清除缓存
            redisCacheService.clearWallpaperCache(cid);
            dataInter.setMsg("删除成功！");
        }else {
            dataInter.setMsg("删除失败！");
        }
        return dataInter;
    }

    //跳转页面
    @GetMapping("/wallpaper/wall_list")
    public ModelAndView getByCid(Integer cid,Model model,ModelAndView mv){
        mv.addObject("cid",cid);
        mv.setViewName("wallpaper/wall_list");
        return mv;
    }

    //后台获取指定分类的壁纸
    @GetMapping("/wallpaper/getWallList")
    @ResponseBody
    public PublicInterface<WallPhoto> getWallLlist(Integer cid,@RequestParam(value = "page",required = false,
            defaultValue = "1") Integer page, @RequestParam(value = "limit",required = false) Integer limit){
        //开启分页
        PageHelper.startPage(page,limit);
        PublicInterface<WallPhoto> res = new PublicInterface<>();
        List<WallPhoto> list = wallpaperService.findPhotoByCid(cid);
        MyData<WallPhoto> data = new MyData<>();
        data.setList(list);
        res.setData(data);
        //封装分页数据
        PageInfo<WallPhoto> pageInfo = new PageInfo<>(list,limit);
        //封装记录条数
        res.setTotal(((Long)pageInfo.getTotal()).intValue());
        return res;
    }


    @LimitRequest
    /*
        微信小程序获取壁纸接口
     */
    @GetMapping("/wallpaper/wall_list/{cid}/{start}/{end}")
    @ResponseBody
    public PublicInterface<PhotoDTO> toList(@PathVariable("cid")Integer cid, @PathVariable("start")Integer start, @PathVariable("end")Integer end){
        //先判断缓存有无信息
        List<PhotoDTO> datalist = redisCacheService.getWallpaperCache(cid, start, end);
        //没有缓存就从数据库中获取
        if (datalist == null){
            datalist = new ArrayList<>();
            int pageSize = end - start + 1 >= 100 ? 100 : end - start + 1;
            //开启分页
            PageHelper.startPage((start/pageSize)+1,pageSize);
            List<Category> catelist = categoryService.findByCid(cid);
            if (catelist.size() != 0){

                //获取图片列表
                List<WallPhoto> photoList = wallpaperService.findPhotoByCid(cid);
                for (WallPhoto wallPhoto : photoList){
                    //获取到的数据转换成DTO对象
                    PhotoDTO photoDTO = new PhotoDTO();
                    BeanUtil.copyProperties(wallPhoto,photoDTO);
                    datalist.add(photoDTO);

                }
            }
            //更新缓存
            redisCacheService.clearWallpaperCache(cid);
            redisCacheService.addWallpaperCache(cid);
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

    //上传壁纸
    @GetMapping("/wallpaper/wall_upload")
    public ModelAndView toUpload(ModelAndView mv){
        List<Category> list = null;
        try {
            list = categoryService.getAll();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        mv.addObject("categorys",list);
        mv.setViewName("wallpaper/wall_upload");
        return mv;
    }

    //统计壁纸图片个数(Echarts)
    @GetMapping("/wallpaper/getTotal")
    @ResponseBody
    public DataInter<Integer> getTotal(){
        int amount = wallpaperService.countTotal();
        DataInter<Integer> res = new DataInter<>();
        res.setData(amount);
        res.setTotal(1);
        return res;
    }

    //计算壁纸各分类的图片数量（Echarts)
    @GetMapping("/wallpaper/getRatio")
    @ResponseBody
    public DataInter<List> getRatio(){
        //获取壁纸的所有分类信息
        List<Category> categoryList = categoryService.getList();
        DataInter<List> res = new DataInter<>();
        List<CategoryRatioModel> categoryRatioModelList = new ArrayList<>();
        //按照分类ID得出该分类的图片数
        for (Category category : categoryList){
            int amount = wallpaperService.countForCount(category.getCid());
            categoryRatioModelList.add(new CategoryRatioModel(amount,category.getTitle()));
        }
        res.setTotal(categoryRatioModelList.size());
        res.setData(categoryRatioModelList);
        return res;
    }

}
