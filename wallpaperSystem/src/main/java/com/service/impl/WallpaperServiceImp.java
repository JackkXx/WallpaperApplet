package com.service.impl;

import com.dao.WallPhotoMapper;
import com.entity.WallPhoto;
import com.service.WallpaperService;
import com.util.qiniu.QiNiuCloudUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class WallpaperServiceImp implements WallpaperService {
    @Autowired
    private QiNiuCloudUtil util;
    @Autowired
    private WallPhotoMapper wallPhotoMapper;
    @Override
    public List<WallPhoto> findPhotoByCid(Integer cid) {
        return wallPhotoMapper.findPhotoByCid(cid);
    }

    @Override
    public boolean add(List<WallPhoto> wallPhotos) {
        return wallPhotoMapper.add(wallPhotos)>0?true:false;
    }

    @Override
    public boolean delete(Integer id,String fileKey) {
        if (wallPhotoMapper.delete(id)>0){
            Boolean flag = false;
            if (fileKey != null && !fileKey.equals("")){
                 flag = util.deleteFile(fileKey);
            }else {
                return true;
            }
            return flag;
        }
        return false;
    }

    @Override
    public boolean addonline(WallPhoto wallPhoto) {
        return wallPhotoMapper.addonline(wallPhoto)>0?true:false;
    }

    @Override
    public int countForCount(Integer cid) {
        return wallPhotoMapper.countForCid(cid);
    }

    @Override
    public int countTotal() {
        return wallPhotoMapper.countTotal();
    }
}
